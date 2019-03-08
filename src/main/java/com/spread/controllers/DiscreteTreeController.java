package com.spread.controllers;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.GsonBuilder;
import com.spread.data.Attribute;
import com.spread.data.AxisAttributes;
import com.spread.data.Layer;
import com.spread.data.Location;
import com.spread.data.SpreadData;
import com.spread.data.TimeLine;
import com.spread.data.attributable.Line;
import com.spread.data.attributable.Point;
import com.spread.data.geojson.GeoJsonData;
import com.spread.data.primitive.Coordinate;
import com.spread.domain.DiscreteAttributeEntity;
import com.spread.domain.DiscreteTreeModelEntity;
import com.spread.domain.IModel;
import com.spread.domain.LocationEntity;
import com.spread.exceptions.SpreadException;
import com.spread.loggers.AbstractLogger;
import com.spread.loggers.ILogger;
import com.spread.loggers.LoggingUtils;
import com.spread.parsers.DiscreteTreeParser;
import com.spread.parsers.GeoJSONParser;
import com.spread.parsers.TimeParser;
import com.spread.repositories.DiscreteTreeModelRepository;
import com.spread.repositories.KeyRepository;
import com.spread.services.ipfs.IpfsService;
import com.spread.services.storage.StorageService;
import com.spread.services.visualization.VisualizationService;
import com.spread.utils.ParsersUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import io.jsonwebtoken.SignatureException;
import jebl.evolution.io.ImportException;
import jebl.evolution.trees.RootedTree;

@Controller
@CrossOrigin
@Transactional
@RequestMapping("/discrete")
public class DiscreteTreeController {

    private AbstractLogger logger;

    @Autowired
    private IpfsService ipfsService;

    @Autowired
    private DiscreteTreeModelRepository modelRepository;

    @Autowired
    private KeyRepository keyRepository;

    @Autowired
    @Qualifier("longRunningTaskExecutor")
    private Executor longRunningTaskExecutor;

    @Autowired
    private VisualizationService visualizationService;

    @Autowired
    private StorageService storageService;

    public DiscreteTreeController() {
    }

    public void init(AbstractLogger logger) {
        this.logger = logger;
    }

    /*
     * This path creates the entity
     * Reuploading tree file with the same sessionId is not possible
     * clients carrying the token should first delete the entity by calling the corresponding endpoint and ask for a new token.
     * */
    @RequestMapping(path = "/tree", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> uploadTree(HttpServletRequest request,
                                             @RequestHeader(value = "Authorization") String authorizationHeader,
                                             @RequestParam(value = "file", required = true) MultipartFile file) {

        String sessionId = "null";

        try {

            String secret = keyRepository.findFirstByOrderByIdDesc().getKey();
            sessionId = ControllerUtils.getSessionId(authorizationHeader, secret);

            if(!(modelRepository.findBySessionId(sessionId) == null)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Session with that id already exists.");
            };

            String filename = file.getOriginalFilename();

            storageService.createSubdirectory(sessionId);
            storageService.store(sessionId, file);

            DiscreteTreeModelEntity model = new DiscreteTreeModelEntity(sessionId, storageService.loadAsResource(sessionId, filename).getFile().getAbsolutePath());

            RootedTree tree = ParsersUtils.importRootedTree(model.getTreeFilename ());

            Set<DiscreteAttributeEntity> attributes = tree.getNodes().stream().filter(node -> !tree.isRoot(node))
                .flatMap(node -> node.getAttributeNames().stream()).map(name -> {
                        return new DiscreteAttributeEntity(name, model);
                    }).collect(Collectors.toSet());

            model.setAttributes(attributes);
            modelRepository.save(model);

            logger.log(ILogger.INFO, "Tree file successfully persisted", new String[][] {
                    {"sessionId", sessionId},
                    {"filename", filename},
                    {"numberOfAttributes", String.valueOf(attributes.size())},
                    {"request-ip" , request.getRemoteAddr()},
                });

            return ResponseEntity.status(HttpStatus.CREATED).header("Location", sessionId).body(ControllerUtils.jsonResponse("OK"));
        } catch (SignatureException e) {
            logger.log(ILogger.ERROR, e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).header("Authorication", "Bearer").body(ControllerUtils.jsonResponse(e.getMessage()));
        } catch (IOException | ImportException e) {
            String message = Optional.ofNullable(e.getMessage()).orElse("Exception encountered when importing tree file");
            logger.log(ILogger.ERROR, e, new String[][] {
                    {"message", message},
                    {"sessionId", sessionId},
                });
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ControllerUtils.jsonResponse("INTERNAL SERVER ERROR"));
        } catch (SpreadException e) {
            logger.log(ILogger.ERROR, e, new String[][] {
                    {"sessionId", sessionId},
                },
                e.getMeta());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).header("Authorication", "Bearer").body(ControllerUtils.jsonResponse("UNAUTHORIZED"));
        }
    }


    @RequestMapping(path = "/geojson", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> uploadGeojson(HttpServletRequest request,
                                                @RequestHeader(value = "Authorization") String authorizationHeader,
                                                @RequestParam(value = "file", required = true) MultipartFile file)  {

        String sessionId = "null";

        try {

            String secret = keyRepository.findFirstByOrderByIdDesc().getKey();
            sessionId = ControllerUtils.getSessionId(authorizationHeader, secret);

            String filename = file.getOriginalFilename();
            if (storageService.exists(sessionId, file)) {
                storageService.delete(sessionId, filename);
                logger.log(ILogger.INFO, "Deleting previously persisted geojson file", new String[][] {
                        {"sessionId", sessionId},
                        {"filename", filename},
                        {"request-ip", request.getRemoteAddr()}
                    });
            }

            storageService.store(sessionId, file);

            DiscreteTreeModelEntity model = modelRepository.findBySessionId(sessionId);
            model.setGeojsonFilename(storageService.loadAsResource(sessionId, filename).getFile().getAbsolutePath());
            modelRepository.save(model);

            logger.log(ILogger.INFO, "geojson file successfully persisted", new String[][] {
                    {"sessionId", sessionId},
                    {"filename", filename},
                    {"request-ip", request.getRemoteAddr()}
                });

            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (SignatureException e) {
            logger.log(ILogger.ERROR, e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ControllerUtils.jsonResponse(e.getMessage()));
        } catch (IOException e) {
            logger.log(ILogger.ERROR, e, new String[][] {
                    {"sessionId", sessionId},
                    {"request-ip", request.getRemoteAddr()}
                });
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ControllerUtils.jsonResponse("INTERNAL SERVER ERROR"));
        } catch (SpreadException e) {
            logger.log(ILogger.ERROR, e, new String[][] {
                    {"sessionId", sessionId},
                },
                e.getMeta());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header("Authorication", "Bearer")
                .body(ControllerUtils.jsonResponse("UNAUTHORIZED"));
        }
    }

    @RequestMapping(path = "/locations", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> setHpdLevel(HttpServletRequest request,
                                              @RequestHeader(value = "Authorization") String authorizationHeader,
                                              @RequestBody (required = true) Set<LocationEntity> locations) {

        String sessionId = "null";

        try {

            String secret = keyRepository.findFirstByOrderByIdDesc().getKey();
            sessionId = ControllerUtils.getSessionId(authorizationHeader, secret);

            DiscreteTreeModelEntity model = modelRepository.findBySessionId(sessionId);
            if(model == null) {
                String message = "Session with that id does not exist";
                logger.log(ILogger.WARN, message, new String[][] {
                        {"sessionId", sessionId},
                        {"request-ip", request.getRemoteAddr()},
                    });
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ControllerUtils.jsonResponse(message));
            };

            locations.forEach((l) -> l.setTree(model));

            model.setLocations(locations);
            modelRepository.save(model);

            logger.log(ILogger.INFO, "Persisted discrete locations", new String[][] {
                    {"sessionId", sessionId},
                    {"request-ip", request.getRemoteAddr()},
                    {"count", String.valueOf (locations.size())},
                });

            return ResponseEntity.status(HttpStatus.OK).body(ControllerUtils.jsonResponse("OK"));
        } catch (SignatureException e) {
            logger.log(ILogger.ERROR, e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).header("Authorication", "Bearer").body(ControllerUtils.jsonResponse("UNATHORIZED"));
        } catch (SpreadException e) {
            logger.log(ILogger.ERROR, e, new String[][] {
                    {"sessionId", sessionId},
                },
                e.getMeta());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).header("Authorication", "Bearer").body(ControllerUtils.jsonResponse("UNAUTHORIZED"));
        }
    }

    @RequestMapping(path = "/location" , method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> setyCoordinates(HttpServletRequest request,
                                                  @RequestHeader(value = "Authorization") String authorizationHeader,
                                                  @RequestParam(value = "value", required = true) String attribute) {

        String sessionId = "null";

        try {

            String secret = keyRepository.findFirstByOrderByIdDesc().getKey();
            sessionId = ControllerUtils.getSessionId(authorizationHeader, secret);

            DiscreteTreeModelEntity model = modelRepository.findBySessionId(sessionId);
            if(model == null) {
                String message = "Session with that id does not exist";
                logger.log(ILogger.WARN, message, new String[][] {
                        {"sessionId", sessionId},
                        {"request-ip", request.getRemoteAddr()}
                    });
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ControllerUtils.jsonResponse(message));
            };

            model.setLocationAttribute(attribute);
            modelRepository.save(model);

            logger.log(ILogger.INFO, "location attribute successfully set", new String[][] {
                    {"sessionId", sessionId},
                    {"attribute", attribute},
                    {"request-ip", request.getRemoteAddr()}
                });

            return ResponseEntity.status(HttpStatus.OK).body(ControllerUtils.jsonResponse("OK"));
        } catch (SignatureException e) {
            logger.log(ILogger.ERROR, e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header("Authorication", "Bearer")
                .body(ControllerUtils.jsonResponse("UNAUTHORIZED"));
        } catch (SpreadException e) {
            logger.log(ILogger.ERROR, e, new String[][] {
                    {"sessionId", sessionId},
                },
                e.getMeta());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header("Authorication", "Bearer")
                .body(ControllerUtils.jsonResponse("UNAUTHORIZED"));
        }
    }

    @RequestMapping(path = "/mrsd", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> setMrsd(HttpServletRequest request,
                                          @RequestHeader(value = "Authorization") String authorizationHeader,
                                          @RequestParam(value = "value") String mrsd) {

        String sessionId = "null";

        try {

            String secret = keyRepository.findFirstByOrderByIdDesc().getKey();
            sessionId = ControllerUtils.getSessionId(authorizationHeader, secret);

            if (!TimeParser.isParseableDate(mrsd)) {
                String message = "Mrsd parameter is in a wrong format";
                logger.log(ILogger.ERROR, message, new String[][] {
                        {"sessionId", sessionId},
                        {"mrsd", mrsd},
                        {"formatToUse", "yyyy" + TimeParser.separator + "MM"
                         + TimeParser.separator + "dd"},
                        {"request-ip", request.getRemoteAddr()}
                    });
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ControllerUtils.jsonResponse(message));
            }

            DiscreteTreeModelEntity model = modelRepository.findBySessionId(sessionId);
            if(model == null) {
                String message = "Session with that id does not exist";
                logger.log(ILogger.WARN, message, new String[][] {
                        {"sessionId", sessionId},
                        {"request-ip", request.getRemoteAddr()}
                    });
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ControllerUtils.jsonResponse(message));
            };

            model.setMrsd(mrsd);
            modelRepository.save(model);

            logger.log(ILogger.INFO, "Mrsd parameter successfully set", new String[][] {
                    {"sessionId", sessionId},
                    {"mrsd", mrsd},
                    {"request-ip", request.getRemoteAddr()}
                });

            return ResponseEntity.status(HttpStatus.OK).body(ControllerUtils.jsonResponse("OK"));
        } catch (SignatureException e) {
            logger.log(ILogger.ERROR, e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).header("Authorication", "Bearer").body(ControllerUtils.jsonResponse(e.getMessage()));
        } catch (SpreadException e) {
            logger.log(ILogger.ERROR, e, new String[][] {
                    {"sessionId", sessionId},
                },
                e.getMeta());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).header("Authorication", "Bearer").body(ControllerUtils.jsonResponse("UNAUTHORIZED"));
        }
    }

    @RequestMapping(path = "/timescale-multiplier", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> setTimescaleMultiplier(HttpServletRequest request,
                                                         @RequestHeader(value = "Authorization") String authorizationHeader,
                                                         @RequestParam(value = "value", required = true) Double timescaleMultiplier) {

        String sessionId = "null";

        try {

            String secret = keyRepository.findFirstByOrderByIdDesc().getKey();
            sessionId = ControllerUtils.getSessionId(authorizationHeader, secret);

            Double min = Double.MIN_NORMAL;
            Double max = Double.MAX_VALUE;

            if (!ControllerUtils.isInInterval(timescaleMultiplier, min, max)) {
                String message = "TimescaleMultiplier value is outside of permitted interval";
                logger.log(ILogger.ERROR, message, new String[][] {
                        {"sessionId", sessionId},
                        {"min", min.toString()},
                        {"max", max.toString()},
                        {"timescaleMultiplier", timescaleMultiplier.toString()},
                        {"request-ip", request.getRemoteAddr()}
                    });
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ControllerUtils.jsonResponse(message));
            }

            DiscreteTreeModelEntity model = modelRepository.findBySessionId(sessionId);
            if(model == null) {
                String message = "Session with that id does not exist";
                logger.log(ILogger.WARN, message, new String[][] {
                        {"sessionId", sessionId},
                        {"request-ip", request.getRemoteAddr()}
                    });
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ControllerUtils.jsonResponse(message));
            };

            model.setTimescaleMultiplier(timescaleMultiplier);
            modelRepository.save(model);

            logger.log(ILogger.INFO, "Timescale multiplier parameter successfully set", new String[][] {
                    {"sessionId", sessionId},
                    {"timescaleMultiplier", timescaleMultiplier.toString()},
                    {"request-ip", request.getRemoteAddr()}
                });

            return ResponseEntity.status(HttpStatus.OK).body(ControllerUtils.jsonResponse("OK"));
        } catch (SignatureException e) {
            logger.log(ILogger.ERROR, e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ControllerUtils.jsonResponse(e.getMessage()));
        } catch (SpreadException e) {
            logger.log(ILogger.ERROR, e, new String[][] {
                    {"sessionId", sessionId},
                },
                e.getMeta());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header("Authorication", "Bearer")
                .body(ControllerUtils.jsonResponse("UNAUTHORIZED"));
        }
    }

    @RequestMapping(path = "/output", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getOutput(HttpServletRequest request,
                                            @RequestHeader(value = "Authorization") String authorizationHeader) {

        String sessionId = "null";

        try {

            String secret = keyRepository.findFirstByOrderByIdDesc().getKey();
            sessionId = ControllerUtils.getSessionId(authorizationHeader, secret);

            DiscreteTreeModelEntity model = modelRepository.findBySessionId(sessionId);
            if(model == null) {
                String message = "Session with that id does not exist";
                logger.log(ILogger.WARN, message, new String[][] {
                        {"sessionId", sessionId},
                        {"request-ip", request.getRemoteAddr()}
                    });
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ControllerUtils.jsonResponse(message));
            };

            final String threadLocalSessionId = sessionId;
            longRunningTaskExecutor.execute(new Runnable() {
                    @Override
                    public void run() {

                        try {

                            logger.log(ILogger.INFO, "Generating output", new String[][] {
                                    {"sessionId", threadLocalSessionId},
                                    {"request-ip", request.getRemoteAddr()}
                                });

                            String json = doGenerateOutput(model);
                            storageService.write(threadLocalSessionId, "data.json", json.getBytes());

                            model.setOutputFilename(storageService.loadAsResource(threadLocalSessionId, "data.json").getFile().getAbsolutePath());
                            model.setStatus(IModel.Status.OUTPUT_READY);
                            modelRepository.save(model);

                            logger.log(ILogger.INFO, "Output succesfully generated", new String[][] {
                                    {"sessionId", threadLocalSessionId},
                                    {"request-ip", request.getRemoteAddr()}
                                });

                        } catch (Exception e) {
                            model.setStatus(IModel.Status.EXCEPTION_OCCURED);
                            modelRepository.save(model);

                            logger.log(ILogger.ERROR, e, new String[][] {
                                    {"sessionId", threadLocalSessionId},
                                    {"request-ip", request.getRemoteAddr()},
                                    {"stacktrace", LoggingUtils.getStackTrace(e)}
                                });
                        }
                    }
                });

            model.setStatus(IModel.Status.GENERATING_OUTPUT);
            modelRepository.save(model);

            logger.log(ILogger.INFO, "PUT /output", new String[][] {
                    {"sessionId", sessionId},
                    {"request-ip" , request.getRemoteAddr()}
                });

            // return immediately
            return ResponseEntity.status(HttpStatus.ACCEPTED).header("Location", sessionId).body(ControllerUtils.jsonResponse("ACCEPTED"));
        } catch (SpreadException | SignatureException e) {
            logger.log(ILogger.ERROR, e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ControllerUtils.jsonResponse("UNAUTHORIZED"));
        }
    }

    @RequestMapping(path = "/ipfs", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> putIpfs(HttpServletRequest request,
                                          @RequestHeader(value = "Authorization") String authorizationHeader) {

        String sessionId = "null";

        try {

            String secret = keyRepository.findFirstByOrderByIdDesc().getKey();
            sessionId = ControllerUtils.getSessionId(authorizationHeader, secret);

            DiscreteTreeModelEntity model = modelRepository.findBySessionId(sessionId);
            if(model == null) {
                String message = "Session with that id does not exist";
                logger.log(ILogger.WARN, message, new String[][] {
                        {"sessionId", sessionId},
                        {"request-ip", request.getRemoteAddr()},
                    });
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ControllerUtils.jsonResponse(message));
            };

            // run in background
            final String threadLocalSessionId = sessionId;

            longRunningTaskExecutor.execute(new Runnable() {
                    @Override
                    public void run() {

                        try {

                            logger.log(ILogger.INFO, "Copying visualisation", new String[][] {
                                    {"sessionId", threadLocalSessionId},
                                });

                            Path source = visualizationService.getVisualisationDirectory();
                            storageService.copy(threadLocalSessionId, source);

                            logger.log(ILogger.INFO, "Copied visualisation", new String[][] {
                                    {"sessionId", threadLocalSessionId},
                                    {"from", source.toString()},
                                    {"to", threadLocalSessionId},
                                });

                            logger.log(ILogger.INFO, "Publishing to ipfs", new String[][] {
                                    {"sessionId", threadLocalSessionId},
                                });

                            String hash = ipfsService.addDirectory(storageService.getSubdirectoryLocation(threadLocalSessionId));
                            model.setIpfsHash(hash);
                            model.setStatus(IModel.Status.IPFS_HASH_READY);
                            modelRepository.save(model);

                            logger.log(ILogger.INFO, "Published to ipfs", new String[][] {
                                    {"sessionId", threadLocalSessionId},
                                    {"ipfsHash", hash},
                                });

                        } catch (Exception e) {
                            model.setStatus(IModel.Status.EXCEPTION_OCCURED);
                            modelRepository.save(model);

                            logger.log(ILogger.ERROR, e, new String[][] {
                                    {"sessionId", threadLocalSessionId},
                                    {"stacktrace", LoggingUtils.getStackTrace(e)}
                                });
                        }
                    }
                });

            // return immediately
            model.setStatus(IModel.Status.PUBLISHING_IPFS);
            modelRepository.save(model);

            logger.log(ILogger.INFO, "PUT /ipfs", new String[][] {
                    {"sessionId", sessionId},
                    {"request-ip" , request.getRemoteAddr()}
                });

            return ResponseEntity.status(HttpStatus.ACCEPTED).header("Location", sessionId).body(ControllerUtils.jsonResponse("ACCEPTED"));
        } catch (SignatureException e) {
            logger.log(ILogger.ERROR, e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ControllerUtils.jsonResponse(e.getMessage()));
        } catch (SpreadException e) {
            logger.log(ILogger.ERROR, e, new String[][] {
                    {"sessionId", sessionId},
                },
                e.getMeta());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ControllerUtils.jsonResponse("UNAUTHORIZED"));
        }
    }

    @RequestMapping(path = "/ipfs", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getIpfsHash(HttpServletRequest request,
                                              @RequestHeader(value = "Authorization") String authorizationHeader) {

        String sessionId = "null";

        try {

            String secret = keyRepository.findFirstByOrderByIdDesc().getKey();
            sessionId = ControllerUtils.getSessionId(authorizationHeader, secret);

            DiscreteTreeModelEntity model = modelRepository.findBySessionId(sessionId);
            if(model == null) {
                String message = "Session with that id does not exist";
                logger.log(ILogger.WARN, message, new String[][] {
                        {"sessionId", sessionId},
                        {"request-ip", request.getRemoteAddr()},
                    });
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ControllerUtils.jsonResponse(message));
            };

            if(!(model.getStatus() == IModel.Status.IPFS_HASH_READY)) {
                String message = "Client should poll for status";
                return ResponseEntity.status(HttpStatus.SEE_OTHER)
                    .header("Location", "/continuous/status")
                    .body(ControllerUtils.jsonResponse(message));
            }

            logger.log(ILogger.INFO, "GET /ipfs", new String[][] {
                    {"sessionId", sessionId},
                    {"request-ip" , request.getRemoteAddr()}
                });

            return ResponseEntity.status(HttpStatus.OK).body(ControllerUtils.jsonResponse(model.getIpfsHash()));
        } catch (SignatureException e) {
            logger.log(ILogger.ERROR, e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ControllerUtils.jsonResponse(e.getMessage()));
        } catch (SpreadException e) {
            logger.log(ILogger.ERROR, e, new String[][] {
                    {"sessionId", sessionId},
                },
                e.getMeta());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header("Authorication", "Bearer")
                .body(ControllerUtils.jsonResponse("UNAUTHORIZED"));
        }
    }

    @RequestMapping(path = "/status", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getStatus(HttpServletRequest request,
                                            @RequestHeader(value = "Authorization") String authorizationHeader) {

        String sessionId = "null";

        try {

            String secret = keyRepository.findFirstByOrderByIdDesc().getKey();
            sessionId = ControllerUtils.getSessionId(authorizationHeader, secret);

            DiscreteTreeModelEntity model = modelRepository.findBySessionId(sessionId);
            if(model == null) {
                String message = "Session with that id does not exist";
                logger.log(ILogger.WARN, message, new String[][] {
                        {"sessionId", sessionId},
                        {"request-ip", request.getRemoteAddr()}
                    });
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ControllerUtils.jsonResponse(message));
            };

            String status = model.getStatus().toString();
            String json = new GsonBuilder().create().toJson(Collections.singletonMap("status", status));

            logger.log(ILogger.INFO, "GET /status", new String[][] {
                    {"sessionId", sessionId},
                    {"status" , status},
                    {"request-ip" , request.getRemoteAddr()},
                });

            return ResponseEntity.status(HttpStatus.OK).body(json);
        } catch (SignatureException e) {
            logger.log(ILogger.ERROR, e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ControllerUtils.jsonResponse(e.getMessage()));
        } catch (SpreadException e) {
            logger.log(ILogger.ERROR, e, new String[][] {
                    {"sessionId", sessionId},
                },
                e.getMeta());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header("Authorication", "Bearer")
                .body(ControllerUtils.jsonResponse("UNAUTHORIZED"));
        }
    }

    @RequestMapping(path = "/model", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DiscreteTreeModelEntity> getModel(HttpServletRequest request,
                                                            @RequestHeader(value = "Authorization") String authorizationHeader) {

        String sessionId = "null";

        try {

            String secret = keyRepository.findFirstByOrderByIdDesc().getKey();
            sessionId = ControllerUtils.getSessionId(authorizationHeader, secret);

            DiscreteTreeModelEntity model = modelRepository.findBySessionId(sessionId);
            if(model == null) {
                String message = "Session with that id does not exist";
                logger.log(ILogger.WARN, message, new String[][] {
                        {"sessionId", sessionId},
                        {"request-ip" , request.getRemoteAddr()}
                    });
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new DiscreteTreeModelEntity());
            };

            logger.log(ILogger.INFO, "GET /model", new String[][] {
                    {"sessionId", sessionId},
                    {"locationAttribute", model.getLocationAttribute()},
                    {"mrsd", model.getMrsd()},
                    {"timescaleMultiplier", Optional.ofNullable(model.getTimescaleMultiplier()).toString()},
                    {"request-ip", request.getRemoteAddr()},
                });

            return ResponseEntity.status(HttpStatus.OK).body(model);
        } catch (SignatureException e) {
            logger.log(ILogger.ERROR, e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (SpreadException e) {
            logger.log(ILogger.ERROR, e, new String[][] {
                    {"sessionId", sessionId},
                },
                e.getMeta());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header("Authorication", "Bearer")
                .body(null);
        }
    }

    private String doGenerateOutput (DiscreteTreeModelEntity model) throws SpreadException, IOException, ImportException {

        TimeLine timeLine = null;
        LinkedList<Attribute> mapAttributes = null;
        LinkedList<Attribute> lineAttributes = null;
        LinkedList<Attribute> pointAttributes = null;
        LinkedList<Location> locationsList = null;

        LinkedList<Layer> layersList = new LinkedList<Layer>();

        // ---IMPORT---//

        RootedTree rootedTree = ParsersUtils.importRootedTree(model.getTreeFilename());

        System.out.println("Imported tree");

        locationsList = model.getLocations().stream().map(l -> {
                return new Location(l.getName(), new Coordinate (l.getLatitude(), l.getLongitude()));
            }).collect(Collectors.toCollection(LinkedList::new));

        System.out.println("Imported locations");

        // ---PARSE AND FILL STRUCTURES---//

        // ---TIME---//

        TimeParser timeParser = new TimeParser(model.getMrsd ());
        timeLine = timeParser.getTimeLine(rootedTree.getHeight(rootedTree.getRootNode()));

        System.out.println("Parsed time line");

        // ---GEOJSON LAYER---//

        if (model.getGeojsonFilename() != null) {

            GeoJSONParser geojsonParser = new GeoJSONParser(model.getGeojsonFilename ());
            GeoJsonData geojson = geojsonParser.parseGeoJSON();

            mapAttributes = geojsonParser.getUniqueMapAttributes();

            String geojsonLayerId = ParsersUtils.splitString(model.getGeojsonFilename (),
                                                             "/");
            Layer geojsonLayer = new Layer(geojsonLayerId, //
                                           "GeoJson layer", //
                                           geojson);

            layersList.add(geojsonLayer);

            System.out.println("Parsed map attributes");

        }// END: null check

        // ---DATA LAYER (POINTS WITH COUNTS)---//

        DiscreteTreeParser treeParser = new DiscreteTreeParser(
                                                               rootedTree,
                                                               model.getLocationAttribute (),
                                                               locationsList,
                                                               timeParser,
                                                               model.getTimescaleMultiplier ()
                                                               );
        treeParser.parseTree();

        LinkedList<Point> countsList = treeParser.getCountsList();

        String countsLayerId = ParsersUtils.splitString(model.getTreeFilename (), "/");
        Layer countsLayer = new Layer(countsLayerId,
                                      "Counts layer",
                                      countsList);

        layersList.add(countsLayer);

        System.out.println("Parsed counts");

        // ---DATA LAYER (TREE LINES & POINTS WITH LOCATIONS)---//

        LinkedList<Line> linesList = treeParser.getLinesList();
        LinkedList<Point> pointsList = treeParser.getPointsList();

        String treeLayerId = ParsersUtils.splitString(model.getTreeFilename (), "/");
        Layer treeLayer = new Layer(treeLayerId,
                                    "Tree layer",
                                    pointsList,
                                    linesList);

        layersList.add(treeLayer);

        System.out.println("Parsed lines and points");

        lineAttributes = treeParser.getLineAttributes();
        pointAttributes = treeParser.getPointAttributes();

        LinkedList<Attribute> rangeAttributes = getCoordinateRangeAttributes(locationsList);
        Attribute xCoordinate = rangeAttributes.get(ParsersUtils.X_INDEX);
        Attribute yCoordinate = rangeAttributes.get(ParsersUtils.Y_INDEX);

        pointAttributes.add(xCoordinate);
        pointAttributes.add(yCoordinate);
        AxisAttributes axis = new AxisAttributes(xCoordinate.getId(),
                                                 yCoordinate.getId());

        System.out.println("Parsed tree attributes");

        SpreadData data = new SpreadData(timeLine,
                                         axis,
                                         mapAttributes,
                                         lineAttributes,
                                         pointAttributes,
                                         null, // areaAttributes
                                         locationsList,
                                         layersList
                                         );

        return new GsonBuilder().create().toJson(data);
    }

    private LinkedList<Attribute> getCoordinateRangeAttributes(LinkedList<Location> locationsList) throws SpreadException  {

        LinkedList<Attribute> coordinateRange = new LinkedList<Attribute>();

        Double[] xCoordinateRange = new Double[2];
        xCoordinateRange[Attribute.MIN_INDEX] = Double.MAX_VALUE;
        xCoordinateRange[Attribute.MAX_INDEX] = Double.MIN_VALUE;

        Double[] yCoordinateRange = new Double[2];
        yCoordinateRange[Attribute.MIN_INDEX] = Double.MAX_VALUE;
        yCoordinateRange[Attribute.MAX_INDEX] = Double.MIN_VALUE;

        for (Location location : locationsList) {

            Coordinate coordinate = location.getCoordinate();
            if (coordinate == null) {
                throw new SpreadException("Location " + location.getId()
                                          + " has no coordinates set.");
            }

            Double latitude = coordinate.getYCoordinate();
            Double longitude = coordinate.getXCoordinate();

            // update coordinates range

            if (latitude < yCoordinateRange[Attribute.MIN_INDEX]) {
                yCoordinateRange[Attribute.MIN_INDEX] = latitude;
            }

            if (latitude > yCoordinateRange[Attribute.MAX_INDEX]) {
                yCoordinateRange[Attribute.MAX_INDEX] = latitude;
            }

            if (longitude < xCoordinateRange[Attribute.MIN_INDEX]) {
                xCoordinateRange[Attribute.MIN_INDEX] = longitude;
            }

            if (longitude > xCoordinateRange[Attribute.MAX_INDEX]) {
                xCoordinateRange[Attribute.MAX_INDEX] = longitude;
            }

        }

        Attribute xCoordinate = new Attribute("xCoordinate", xCoordinateRange);
        Attribute yCoordinate = new Attribute("yCoordinate", yCoordinateRange);

        coordinateRange.add(ParsersUtils.X_INDEX, xCoordinate);
        coordinateRange.add(ParsersUtils.Y_INDEX, yCoordinate);

        return coordinateRange;
    }

}
