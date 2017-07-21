/* Generated By:JavaCC: Do not edit this line. TransformLangParserVisitor.java Version 5.0 */
package org.jetel.ctl;

import org.jetel.ctl.ASTnode.*;

public interface TransformLangParserVisitor extends SyntheticNodeVisitor
{
  public Object visit(SimpleNode node, Object data);
  public Object visit(CLVFStart node, Object data);
  public Object visit(CLVFStartExpression node, Object data);
  public Object visit(CLVFImportSource node, Object data);
  public Object visit(CLVFParameters node, Object data);
  public Object visit(CLVFType node, Object data);
  public Object visit(CLVFFunctionDeclaration node, Object data);
  public Object visit(CLVFVariableDeclaration node, Object data);
  public Object visit(CLVFAssignment node, Object data);
  public Object visit(CLVFConditionalFailExpression node, Object data);
  public Object visit(CLVFConditionalExpression node, Object data);
  public Object visit(CLVFOr node, Object data);
  public Object visit(CLVFAnd node, Object data);
  public Object visit(CLVFComparison node, Object data);
  public Object visit(CLVFAddNode node, Object data);
  public Object visit(CLVFSubNode node, Object data);
  public Object visit(CLVFMulNode node, Object data);
  public Object visit(CLVFDivNode node, Object data);
  public Object visit(CLVFModNode node, Object data);
  public Object visit(CLVFUnaryStatement node, Object data);
  public Object visit(CLVFUnaryNonStatement node, Object data);
  public Object visit(CLVFPostfixExpression node, Object data);
  public Object visit(CLVFMemberAccessExpression node, Object data);
  public Object visit(CLVFArrayAccessExpression node, Object data);
  public Object visit(CLVFInFunction node, Object data);
  public Object visit(CLVFFunctionCall node, Object data);
  public Object visit(CLVFIsNullNode node, Object data);
  public Object visit(CLVFNVLNode node, Object data);
  public Object visit(CLVFNVL2Node node, Object data);
  public Object visit(CLVFIIfNode node, Object data);
  public Object visit(CLVFPrintErrNode node, Object data);
  public Object visit(CLVFPrintLogNode node, Object data);
  public Object visit(CLVFPrintStackNode node, Object data);
  public Object visit(CLVFRaiseErrorNode node, Object data);
  public Object visit(CLVFFieldAccessExpression node, Object data);
  public Object visit(CLVFIdentifier node, Object data);
  public Object visit(CLVFArguments node, Object data);
  public Object visit(CLVFDateField node, Object data);
  public Object visit(CLVFLogLevel node, Object data);
  public Object visit(CLVFLiteral node, Object data);
  public Object visit(CLVFListOfLiterals node, Object data);
  public Object visit(CLVFBlock node, Object data);
  public Object visit(CLVFIfStatement node, Object data);
  public Object visit(CLVFSwitchStatement node, Object data);
  public Object visit(CLVFCaseStatement node, Object data);
  public Object visit(CLVFWhileStatement node, Object data);
  public Object visit(CLVFForStatement node, Object data);
  public Object visit(CLVFForeachStatement node, Object data);
  public Object visit(CLVFDoStatement node, Object data);
  public Object visit(CLVFBreakStatement node, Object data);
  public Object visit(CLVFContinueStatement node, Object data);
  public Object visit(CLVFReturnStatement node, Object data);
  public Object visit(CLVFSequenceNode node, Object data);
  public Object visit(CLVFLookupNode node, Object data);
  public Object visit(CLVFDictionaryNode node, Object data);
}
/* JavaCC - OriginalChecksum=46eab910681949722e5e0548f060ff48 (do not edit this line) */
