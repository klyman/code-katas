package info.szadkowski.katas.zigzag;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BinaryTreeParserTest {
  private BinaryTreeParser<Node> parser;

  @BeforeEach
  void setUp() {
    parser = new BinaryTreeParser<>(Node::new);
  }

  @Nested
  class ErrorCases {

    @Test
    void shouldThrowOnNullInput() {
      BinaryTreeParser.InvalidInputException exception = Assertions.assertThrows(BinaryTreeParser.InvalidInputException.class, () -> {
        parser.parse(null);
      });

      assertThat(exception).hasMessage("Input cannot be null");
    }

    @Test
    void shouldThrowOnEmptyInput() {
      BinaryTreeParser.InvalidInputException exception = Assertions.assertThrows(BinaryTreeParser.InvalidInputException.class, () -> {
        parser.parse("");
      });

      assertThat(exception).hasMessage("Cannot find any node");
    }

    @Test
    void shouldFailOnMissingNodeIndicatorOnLeft() {
      BinaryTreeParser.InvalidInputException exception = Assertions.assertThrows(BinaryTreeParser.InvalidInputException.class, () -> {
        parser.parse("-()");
      });

      assertThat(exception).hasMessage("Expected '+' in (line 0, position 0)");
    }

    @Test
    void shouldFailOnMissingNodeIndicatorOnLeftRight() {
      BinaryTreeParser.InvalidInputException exception = Assertions.assertThrows(BinaryTreeParser.InvalidInputException.class, () -> {
        parser.parse("()-");
      });

      assertThat(exception).hasMessage("Expected '+' in (line 0, position 3)");
    }

    @Test
    void shouldRequireNewLineWithLeftNodeIfStated() {
      BinaryTreeParser.InvalidInputException exception = Assertions.assertThrows(BinaryTreeParser.InvalidInputException.class, () -> {
        parser.parse("+-()");
      });

      assertThat(exception).hasMessage("Expected 2 line");
    }

    @Test
    void shouldRequireNewLineWithRightNodeIfStated() {
      BinaryTreeParser.InvalidInputException exception = Assertions.assertThrows(BinaryTreeParser.InvalidInputException.class, () -> {
        parser.parse("()-+");
      });

      assertThat(exception).hasMessage("Expected 2 line");
    }

    @Test
    void shouldRequireLeftNodeBeginIfStated() {
      BinaryTreeParser.InvalidInputException exception = Assertions.assertThrows(BinaryTreeParser.InvalidInputException.class, () -> {
        parser.parse("+-()\n");
      });

      assertThat(exception).hasMessage("Expected node begin in (line 1, position 0)");
    }

    @Test
    void shouldRequireRightNodeBeginIfStated() {
      BinaryTreeParser.InvalidInputException exception = Assertions.assertThrows(BinaryTreeParser.InvalidInputException.class, () -> {
        parser.parse("()-+\n");
      });

      assertThat(exception).hasMessage("Expected node begin in (line 1, position 3)");
    }

    @Test
    void shouldRequireLeftNodeEndIfStated() {
      BinaryTreeParser.InvalidInputException exception = Assertions.assertThrows(BinaryTreeParser.InvalidInputException.class, () -> {
        parser.parse("+-()\n" +
                     "(");
      });

      assertThat(exception).hasMessage("Expected node end in (line 1, position 1)");
    }

    @Test
    void shouldRequireRightNodeEndIfStated() {
      BinaryTreeParser.InvalidInputException exception = Assertions.assertThrows(BinaryTreeParser.InvalidInputException.class, () -> {
        parser.parse("()-+\n" +
                     "   (");
      });

      assertThat(exception).hasMessage("Expected node end in (line 1, position 4)");
    }
  }

  @Test
  void shouldConvertOneNode() {
    Node tree = parser.parse("()");

    assertThat(tree).isNotNull();
    assertThat(tree.left).isNull();
    assertThat(tree.right).isNull();
    assertThat(tree.content).isEmpty();
  }

  @Test
  void shouldConvertOneNodeWithAdditionalBlankChars() {
    Node tree = parser.parse("    ()  ");

    assertThat(tree).isNotNull();
    assertThat(tree.left).isNull();
    assertThat(tree.right).isNull();
    assertThat(tree.content).isEmpty();
  }

  @Test
  void shouldConvertOneNodeWithContent() {
    Node tree = parser.parse("(content)");

    assertThat(tree).isNotNull();
    assertThat(tree.left).isNull();
    assertThat(tree.right).isNull();
    assertThat(tree.content).isEqualTo("content");
  }

  @Test
  void shouldSkipEmptyTopLines() {
    Node tree = parser.parse("\n  \t \n\n   (content)");

    assertThat(tree).isNotNull();
    assertThat(tree.left).isNull();
    assertThat(tree.right).isNull();
    assertThat(tree.content).isEqualTo("content");
  }

  @Test
  void shouldReadLeftNode() {
    Node tree = parser.parse("+-(content)\n" +
                             "(left)");

    assertThat(tree).isNotNull();
    assertThat(tree.left).isNotNull();
    assertThat(tree.right).isNull();
    assertThat(tree.content).isEqualTo("content");
    assertThat(tree.left.left).isNull();
    assertThat(tree.left.right).isNull();
    assertThat(tree.left.content).isEqualTo("left");
  }

  @Test
  void shouldReadRightNode() {
    Node tree = parser.parse("(content)--+\n" +
                             "           (right)");

    assertThat(tree).isNotNull();
    assertThat(tree.left).isNull();
    assertThat(tree.right).isNotNull();
    assertThat(tree.content).isEqualTo("content");
    assertThat(tree.right.left).isNull();
    assertThat(tree.right.right).isNull();
    assertThat(tree.right.content).isEqualTo("right");
  }

  @Test
  void integration() {
    Node tree = parser.parse("         +-------(root)--+\n" +
                             "   +--(left1)-+       (right1)---+\n" +
                             "(left2)    (right2)           (right3)");

    assertThat(tree.content).isEqualTo("root");
    assertThat(tree.left.content).isEqualTo("left1");
    assertThat(tree.right.content).isEqualTo("right1");

    assertThat(tree.left.left.content).isEqualTo("left2");
    assertThat(tree.left.left.left).isNull();
    assertThat(tree.left.left.right).isNull();

    assertThat(tree.left.right.content).isEqualTo("right2");
    assertThat(tree.left.right.left).isNull();
    assertThat(tree.left.right.right).isNull();

    assertThat(tree.right.left).isNull();
    assertThat(tree.right.right.content).isEqualTo("right3");
    assertThat(tree.right.right.left).isNull();
    assertThat(tree.right.right.right).isNull();
  }

  static class Node {
    final Node left;
    final Node right;
    final String content;

    Node(Node left, Node right, String content) {
      this.left = left;
      this.right = right;
      this.content = content;
    }
  }
}
