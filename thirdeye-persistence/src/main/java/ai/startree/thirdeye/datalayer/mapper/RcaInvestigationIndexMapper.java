package ai.startree.thirdeye.datalayer.mapper;

import ai.startree.thirdeye.datalayer.entity.RcaInvestigationIndex;
import ai.startree.thirdeye.spi.datalayer.dto.RcaInvestigationDTO;
import org.modelmapper.PropertyMap;

/**
 * Custom mapping from DTO to Index.
 * See http://modelmapper.org/javadoc/ --> PropertyMap
 */
public class RcaInvestigationIndexMapper extends
    PropertyMap<RcaInvestigationDTO, RcaInvestigationIndex> {

  @Override
  protected void configure() {
    // make sure to read the doc - below is not usual java - it's using an embedded DSL
    map().setAnomalyId(source.getAnomaly().getId());
    map().setAnomalyRangeStart(source.getAnomaly().getStartTime());
    map().setAnomalyRangeEnd(source.getAnomaly().getEndTime());
    map().setOwner(source.getCreatedBy());
    map().setCreated(source.getCreateTime().getTime());
    map().setUpdated(source.getUpdateTime().getTime());
  }
}
