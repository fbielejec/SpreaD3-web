package com.spread.services.ipfs;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.springframework.stereotype.Service;

import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;

@Service
public class HttpIpfsService implements IpfsService {

    private IPFS ipfs;
    private Boolean isInit = false;

    @Override
    public void init(String ipfsHost) {
        ipfs = new IPFS(ipfsHost);
        isInit = true;
    }

    /**
     * @return the isInit
     */
    @Override
    public Boolean isInitialized() {
        return isInit;
    }

    @Override
    public String addDirectory(Path directory) throws IOException {
        List<MerkleNode> addParts = ipfs.add(new NamedStreamable.FileWrapper(directory.toFile()));
        MerkleNode addResult = addParts.get(addParts.size() - 1);
        // List<MerkleNode> lsResult = ipfs.ls(addResult.hash);
        return addResult.hash.toString();
    }

}
