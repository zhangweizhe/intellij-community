package com.jetbrains.python.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReference;
import com.jetbrains.python.PyElementTypes;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.PyTokenTypes;
import com.jetbrains.python.PythonDialectsTokenSetProvider;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.references.PyOperatorReference;
import com.jetbrains.python.psi.resolve.PyResolveContext;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yole
 */
public class PyPrefixExpressionImpl extends PyElementImpl implements PyPrefixExpression {
  public PyPrefixExpressionImpl(ASTNode astNode) {
    super(astNode);
  }

  @Override
  public PyExpression getOperand() {
    return (PyExpression)childToPsi(PythonDialectsTokenSetProvider.INSTANCE.getExpressionTokens(), 0);
  }

  @Nullable
  public PsiElement getPsiOperator() {
    final ASTNode node = getNode();
    final ASTNode child = node.findChildByType(PyElementTypes.UNARY_OPS);
    return child != null ? child.getPsi() : null;
  }

  @NotNull
  @Override
  public PyElementType getOperator() {
    final PsiElement op = getPsiOperator();
    assert op != null;
    return (PyElementType)op.getNode().getElementType();
  }

  @Override
  protected void acceptPyVisitor(PyElementVisitor pyVisitor) {
    pyVisitor.visitPyPrefixExpression(this);
  }

  @Override
  public PsiReference getReference() {
    return getReference(PyResolveContext.noImplicits());
  }

  @Override
  public PsiPolyVariantReference getReference(PyResolveContext context) {
    final PyElementType t = getOperator();
    if (t.getSpecialMethodName() != null) {
      return new PyOperatorReference(this, context);
    }
    return null;
  }

  @Override
  public PyType getType(@NotNull TypeEvalContext context) {
    final PsiReference ref = getReference(PyResolveContext.noImplicits().withTypeEvalContext(context));
    if (ref != null) {
      final PsiElement resolved = ref.resolve();
      if (resolved instanceof Callable) {
        return ((Callable)resolved).getReturnType(context, null);
      }
    }
    return null;
  }

  @Override
  public PyExpression getQualifier() {
    return getOperand();
  }

  @Override
  public String getReferencedName() {
    PyElementType t = getOperator();
    if (t == PyTokenTypes.PLUS) {
      return PyNames.POS;
    }
    else if (t == PyTokenTypes.MINUS) {
      return PyNames.NEG;
    }
    return getOperator().getSpecialMethodName();
  }

  @Override
  public ASTNode getNameElement() {
    final PsiElement op = getPsiOperator();
    return op != null ? op.getNode() : null;
  }
}
