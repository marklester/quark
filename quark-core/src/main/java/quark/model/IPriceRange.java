package quark.model;

import java.math.BigDecimal;


public interface IPriceRange {

  BigDecimal getLow();

  BigDecimal getHigh();

  Integer getTpId();

}
