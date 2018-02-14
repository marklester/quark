package quark.orders;

import com.google.common.base.MoreObjects;

public class ProcessedOrder implements Comparable<ProcessedOrder> {
  public static ProcessedOrder failed(AlgoOrder order,String message) {
    return new ProcessedOrder(order, false, null,message);
  }
  
  private final AlgoOrder order;
  private final boolean success;
  private final String message;
  private final Receipt receipt;

  public ProcessedOrder(AlgoOrder order, boolean success, Receipt receipt) {
    this(order,success,receipt,null);
  }
  
  public ProcessedOrder(AlgoOrder order, boolean success, Receipt receipt,String message) {
    this.order = order;
    this.success = success;
    this.receipt = receipt;
    this.message = message;
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

  @Override
  public int compareTo(ProcessedOrder other) {
    return other.getOrder().compareTo(other.getOrder());
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((order == null) ? 0 : order.hashCode());
    result = prime * result + ((receipt == null) ? 0 : receipt.hashCode());
    result = prime * result + (success ? 1231 : 1237);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ProcessedOrder other = (ProcessedOrder) obj;
    if (order == null) {
      if (other.order != null)
        return false;
    } else if (!order.equals(other.order))
      return false;
    if (receipt == null) {
      if (other.receipt != null)
        return false;
    } else if (!receipt.equals(other.receipt))
      return false;
    if (success != other.success)
      return false;
    return true;
  }

  public String getMessage() {
    return message;
  }
}

