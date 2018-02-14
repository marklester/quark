package quark.populator;

class Position {
  final int position;
  final int total;

  public Position(int position, int total) {
    this.position = position;
    this.total = total;
  }

  public int getPosition() {
    return position;
  }

  public int getTotal() {
    return total;
  }

  public String toString() {
    return String.format("(%s/%s)", position, total);
  }
}