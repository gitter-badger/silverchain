package matrix;

public final class Matrix<R extends Size, C extends Size> extends Matrix0<R, C> {

  Matrix() {
    super(new MatrixAction<>());
  }
}
