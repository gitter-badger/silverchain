package silverchain.grammar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

abstract class ASTNodeN<T, S extends ASTNodeN<T, S>> extends ASTNode2<T, S> implements Iterable<T> {

  abstract String separator();

  ASTNodeN(T head, S tail) {
    super(head, tail);
  }

  public final T head() {
    return left();
  }

  public final S tail() {
    return right();
  }

  @Override
  public final Iterator<T> iterator() {
    List<T> list = new ArrayList<>();
    ASTNodeN<T, S> node = this;
    while (node != null) {
      list.add(node.head());
      node = node.tail();
    }
    return list.iterator();
  }

  @Override
  public final String toString() {
    List<String> list = new ArrayList<>();
    forEach(node -> list.add(node.toString()));
    return String.join(separator(), list);
  }
}
