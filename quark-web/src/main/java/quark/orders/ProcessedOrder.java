package quark.orders;

import com.google.common.base.MoreObjects;

public class ProcessedOrder {
  
  public static ProcessedOrder failed(AlgoOrder order,String message) {
    return new ProcessedOrder(order, false, null);
  }
  
  private final AlgoOrder order;
  private final boolean success;
  private final Receipt receipt;

  public ProcessedOrder(AlgoOrder order, boolean success, Receipt receipt) {
    this.order = order;
    this.success = success;
    this.receipt = receipt;
  }

  public AlgoOrder getOrder() {
    return order;
  }

  public boolean isSuccess() {
    return success;
  }

  public String toString() {
    return MoreObjects.toStringHelper(this).add("success", success).add("order", order)
        .add("receipt", getReceipt()).toString();
  }

  public Receipt getReceipt() {
    return receipt;
  }
}

