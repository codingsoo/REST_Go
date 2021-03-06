package org.cbioportal.genome_nexus.persistence.internal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class IndexRepositoryImpl extends JsonMongoRepositoryImpl
{
    public static final String COLLECTION = "index";

    @Autowired
    public IndexRepositoryImpl(MongoTemplate mongoTemplate)
    {
        super(mongoTemplate);
    }
}
