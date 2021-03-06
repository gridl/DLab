package com.epam.dlab.backendapi.dao.gcp;

import com.epam.dlab.backendapi.dao.KeyDAO;
import com.epam.dlab.dto.gcp.edge.EdgeInfoGcp;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Singleton
public class GcpKeyDao extends KeyDAO {
    public GcpKeyDao() {
        log.info("{} is initialized", getClass().getSimpleName());
    }

    @Override
    public EdgeInfoGcp getEdgeInfo(String user) {
        return super.getEdgeInfo(user, EdgeInfoGcp.class, new EdgeInfoGcp());
    }
}
