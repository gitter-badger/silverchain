package silverchain.validator;

import static java.lang.String.join;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import silverchain.diagram.Diagram;
import silverchain.diagram.Diagrams;
import silverchain.diagram.Label;
import silverchain.diagram.State;
import silverchain.diagram.Transition;
import silverchain.parser.FormalParameter;
import silverchain.parser.FormalParameters;
import silverchain.parser.Method;
import silverchain.parser.Range;
import silverchain.parser.TypeReference;

public class JavaValidator extends Validator {

  public JavaValidator(Diagrams diagrams) {
    super(diagrams);
  }

  @Override
  protected void validate(Diagrams diagrams) {
    diagrams.forEach(this::validate);
  }

  private void validate(Diagram diagram) {
    diagram.states().forEach(this::validate);
  }

  private void validate(State state) {
    checkTypeReferenceConflict(state);
    checkTypeReferenceMethodConflict(state);
    checkMethodConflict(state);
  }

  private void checkTypeReferenceConflict(State state) {
    List<Label> labels = state.typeReferences();
    if (1 < labels.size()) {
      throwError(labels);
    }
  }

  private void checkTypeReferenceMethodConflict(State state) {
    List<Label> labels = state.typeReferences();
    List<Transition> transitions = state.transitions();
    if (0 < labels.size() && 0 < transitions.size()) {
      Stream<Label> s1 = labels.stream();
      Stream<Label> s2 = transitions.stream().map(Transition::label);
      throwError(concat(s1, s2).collect(toList()));
    }
  }

  private void checkMethodConflict(State state) {
    for (List<Label> labels : getLabelGroups(state)) {
      if (1 < labels.size()) {
        throwError(labels);
      }
    }
  }

  private void throwError(List<Label> labels) {
    throw new ValidationError("Conflict: %s", stringify(labels));
  }

  private String stringify(Collection<Label> labels) {
    return labels.stream().map(this::stringify).collect(joining(", "));
  }

  private String stringify(Label label) {
    return label.node().toString() + "#" + stringify(label.ranges());
  }

  private String stringify(List<Range> ranges) {
    return ranges.stream().map(this::stringify).collect(joining(","));
  }

  private String stringify(Range range) {
    return range.begin().toString();
  }

  private Collection<List<Label>> getLabelGroups(State state) {
    return state.transitions().stream()
        .map(Transition::label)
        .collect(groupingBy(this::getSignature))
        .values();
  }

  private String getSignature(Label label) {
    return getSignature(label.method());
  }

  private String getSignature(Method method) {
    String s = method.parameters().formalParameters().map(this::getSignature).orElse("");
    return method.name() + ":" + s;
  }

  private String getSignature(FormalParameters parameters) {
    return parameters.stream().map(this::getSignature).collect(joining(" "));
  }

  private String getSignature(FormalParameter parameter) {
    String s1 = getSignature(parameter.type());
    String s2 = parameter.isVarArgs() ? "[]" : "";
    return s1 + s2;
  }

  private String getSignature(TypeReference reference) {
    String s1 = reference.referent() == null ? join(".", reference.name()) : "Object";
    String s2 = reference.isArray() ? "[]" : "";
    return s1 + s2;
  }
}
