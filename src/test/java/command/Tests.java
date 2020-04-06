package command;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Tests {

  private static final Path resources = Paths.get("src").resolve("test").resolve("resources");

  private static final Path workspace = Paths.get("build").resolve("silverchain");

  @Test
  void testHelp() {
    String help =
        "Usage: silverchain [options]\n"
            + "\n"
            + "options:\n"
            + "  -h, --help             Show this message and exit\n"
            + "  -v, --version          Show version and exit\n"
            + "  -i, --input <path>     Input grammar file\n"
            + "  -o, --output <path>    Output directory\n"
            + "  -l, --language <lang>  Output language\n";
    test("-h").status(0).stdout(help).stderr("");
    test("--help").status(0).stdout(help).stderr("");
  }

  @Test
  void testVersion() {
    String version = findVersion() + "\n";
    test("-v").status(0).stdout(version).stderr("");
    test("--version").status(0).stdout(version).stderr("");
  }

  @Test
  void testUnknownOption() {
    test("-foo").status(101).stdout("").stderr("Unknown option: -foo\n");
  }

  @Test
  void testUnsupportedLanguage() {
    test("-l", "foo").status(102).stdout("").stderr("Unsupported language: foo\n");
  }

  @Test
  void testInputError1() {
    test("-i", "foo.ag").status(103).stdout("").stderr("File not found: foo.ag\n");
    test("--input", "foo.ag").status(103).stdout("").stderr("File not found: foo.ag\n");
  }

  @Test
  void testInputError2() {
    System.setIn(new BrokenInputStream("Foo:"));
    test("-o", workspace.toString()).status(103).stdout("");
  }

  @Test
  void testTokenizeError() {
    input("=");
    test("-o", workspace.toString()).status(104).stdout("");
  }

  @Test
  void testParseError() {
    input(":");
    test("-o", workspace.toString()).status(105).stdout("");
  }

  @Test
  void testDuplicateDeclaration() {
    input("Foo[T,T]:");
    test("-o", workspace.toString()).status(106).stdout("").stderr("T is already defined (L1C7)\n");
  }

  @Test
  void testSaveError() {
    input("Foo: foo();");
    test("-o", "build.gradle")
        .status(108)
        .stdout("")
        .stderr("Failed to save generated file: build.gradle/IFoo.java\n");
  }

  @Test
  void testSuccessStdin() {
    input("Foo: foo() Bar;");
    test("-o", workspace.toString()).status(0).stdout("").stderr("");

    input("Foo: foo() Bar;");
    test("--output", workspace.toString()).status(0).stdout("").stderr("");
  }

  @Test
  void testSuccessFile() {
    test("-i", resources.resolve("mapbuilder.ag").toString(), "-o", workspace.toString())
        .status(0)
        .stdout("")
        .stderr("");
  }

  private CommandTester test(String... args) {
    return new CommandTester(args);
  }

  private void input(String text) {
    System.setIn(new ByteArrayInputStream(text.getBytes()));
  }

  private String findVersion() {
    return readBuildGradle()
        .filter(s -> s.startsWith("version "))
        .map(s -> s.split(" ")[1].replaceAll("'", "").trim())
        .findFirst()
        .orElse(null);
  }

  private Stream<String> readBuildGradle() {
    try {
      return Files.readAllLines(Paths.get("build.gradle")).stream();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @BeforeEach
  void renewWorkspace() {
    try {
      delete(workspace.toFile());
      Files.createDirectories(workspace);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  private static void delete(File file) {
    if (!file.exists()) {
      return;
    }
    File[] files = file.listFiles();
    if (files != null) {
      for (File f : files) {
        delete(f);
      }
    }
    file.delete();
  }
}
