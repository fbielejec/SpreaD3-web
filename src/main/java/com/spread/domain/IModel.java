package com.spread.domain;


public interface IModel {

        public enum Status  {
        EXCEPTION_OCCURED,
        GENERATING_OUTPUT, OUTPUT_READY,
        PUBLISHING_IPFS, IPFS_HASH_READY
    }


}
