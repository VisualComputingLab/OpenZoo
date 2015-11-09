package gr.iti.openzoo.pojos;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
public class Triple<L,M,R> {

  private final L left;
  private final M middle;
  private final R right;

  public Triple(L left, M middle, R right) {
    this.left = left;
    this.middle = middle;
    this.right = right;
  }

  public L getLeft() { return left; }
  public M getMiddle() { return middle; }
  public R getRight() { return right; }

  @Override
  public int hashCode() { return (left.hashCode() ^ middle.hashCode() ^ right.hashCode()) % Integer.MAX_VALUE; }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Triple)) return false;
    Triple triplo = (Triple) o;
    return this.left.equals(triplo.getLeft()) &&
           this.middle.equals(triplo.getMiddle()) &&
           this.right.equals(triplo.getRight());
  }

  @Override
  public String toString()
  {
      return "Triple ( " + getLeft().toString() + ", " + getMiddle().toString() + ", " + getRight().toString() + " )";
  }
}
