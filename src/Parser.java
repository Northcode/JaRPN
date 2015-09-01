// Reverse polish notation parser written in Java
// Andreas Larsen
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;
import java.util.List;
import java.util.Arrays;
import java.util.Random;

class Parser {

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		Parser parser = new Parser();
		while (true) {
			System.out.print("\n>");
			String line = sc.nextLine();
			parser.parse(line);
			try {
				parser.runInterpreter();
			} catch (Exception e) {
				System.out.println("An error occured while preforming operation: " + e);
			}
		        parser.rpn.clear();
		}
	}

	StringReader sr;

	// compiler memory
	public ArrayList<Object> rpn;
	public List<String> functionNames;

	// runtime memory
	public Map<String, Object> variables;
	public Stack<Object> compStack;
	Random randgen;
	
	public Parser() {
		rpn = new ArrayList<Object>();
		variables = new HashMap<String, Object>();
		variables.put("pi",Math.PI);
		compStack = new Stack<Object>();
		randgen = new Random();
		functionNames = Arrays.asList("assign","sin","cos","tan","acos","acos","atan","sqrt","random","vars");
	}

	public Double unwrapVar(String varname) throws Exception {
		if (variables.get(varname) instanceof String) {
			if (varname == (String)variables.get(varname)) {
				throw new Exception("Variable " + varname + " is self referencing, cannot unwrap");
			}
			return unwrapVar((String)variables.get(varname));
		} else if (variables.get(varname) instanceof Double) {
			return (Double)variables.get(varname);
		} else {
			throw new Exception("Could not unwrap variable, not found or not a number");
		}
	}
	
	public void runInterpreter() throws Exception {
		int ip = 0;
		while (ip < rpn.size()) {
			//System.out.print(rpn.get(ip) + " ");
			if (rpn.get(ip) instanceof String) {
				String str = (String)rpn.get(ip);
				if (str.equals("assign")) {
					Object value = compStack.pop();
					String varname = (String)compStack.pop();
					variables.put(varname,value);
				} else if (str.equals("vars")) {
					System.out.println("Current Variables: ");
					for (Map.Entry<String,Object> v : variables.entrySet()) {
						try {
						System.out.println(v.getKey() + " = " + v.getValue() + "  unwraps to: " + unwrapVar(v.getKey()));
						} catch (Exception e) { throw e; }
					}
				} else if (str.equals("random")) {
					compStack.push(randgen.nextDouble());
				} else if (functionNames.contains(str)) {
					Object value = compStack.pop();
					Double val = 0.0;

					if(value instanceof Double) {
						val = (Double)value;
					} else if (value instanceof String) {
						String varname = (String)value;
						if (variables.containsKey(varname)) {
							try { val = unwrapVar(varname); } catch (Exception e) { throw e; }
						} else {
							throw new Exception("Attempted to use an unassigned variable '" + varname + "' in function " + str);
						}
					} else {
						throw new Exception("Cannot preform " + str + " on a non number");
					}
					
					Double res = 0.0;
					
					switch (str) {
					case "sin":
						res = Math.sin(val);
						break;
					case "cos":
						res = Math.cos(val);
						break;
					case "tan":
						res = Math.tan(val);
						break;
					case "asin":
						res = Math.asin(val);
						break;
					case "acos":
						res = Math.acos(val);
						break;
					case "atan":
						res = Math.atan(val);
						break;
					case "sqrt":
						res = Math.sqrt(val);
						break;
					}

					compStack.push(res);
				} else {
					compStack.push(str);
				}
			} else if (rpn.get(ip) instanceof Character) {
				char op = (Character)rpn.get(ip);
			        if (op == '=') {
					Object value = compStack.pop();
					String varname = (String)compStack.pop();
					variables.put(varname,value);
				} else {
					Object obj2 = compStack.pop();
					Object obj1 = compStack.pop();

					Double val1 = 0.0;
					Double val2 = 0.0;

				        if(obj1 instanceof Double) {
						val1 = (Double)obj1;
					} else if (obj1 instanceof String) {
						String varname = (String)obj1;
						if (variables.containsKey(varname)) {
							try { val1 = unwrapVar(varname); } catch (Exception e) { throw e; }
						} else {
							throw new Exception("Attempted to use an unassigned variable " + varname);
						}
					} else {
						throw new Exception("Cannot preform " + op + " on a non number");
					}

					if(obj2 instanceof Double) {
						val2 = (Double)obj2;
					} else if (obj2 instanceof String) {
						String varname = (String)obj2;
						if (variables.containsKey(varname)) {
							try { val2 = unwrapVar(varname); } catch (Exception e) { throw e; }
						} else {
							throw new Exception("Attempted to use an unassigned variable " + varname);
						}
					} else {
						throw new Exception("Cannot preform " + op + " on a non number");
					}

					Double res = 0.0;

					switch (op) {
					case '+':
						res = val1 + val2;
						break;
					case '-':
						res = val1 - val2;
						break;
					case '*':
						res = val1 * val2;
						break;
					case '/':
						res = val1 / val2;
						break;
					case '^':
						res = Math.pow(val1,val2);
						break;
					}

					compStack.push(res);
				}
			} else if (rpn.get(ip) instanceof Double) {
				compStack.push((Double)rpn.get(ip));
			}
			ip++;
		}

		while (!compStack.empty()) {
			System.out.print(compStack.pop() + " ");
		}

		rpn.clear();
	}
	
	final int functionPri = 4;
	
	public int operatorPri(char op) {
		switch (op) {
		case '+':
		case '-':
			return 1;
		case '*':
		case '/':
			return 2;
		case '^':
			return 3;
		case '(':
		case ',':
			return 5;
		case '=':
		default:
			return 0;
		}
	}

	public boolean isRightAssoc(char op) {
	        return op == '^';
	}

	public void parse(String line) {
		sr = new StringReader(line);
		int i = 0;
		Character c = '\0';
		Stack<Object> operators = new Stack<Object>();
		try {
			i = sr.read();
			while(i != -1) {
				c = (char)i;
				if (Character.isLetter(c)) {
					String str = new String();
					while (Character.isLetter(c) || Character.isDigit(c)) {
						str += c;
						i = sr.read(); c = (char)i;
					}
					if (functionNames.contains(str)) {
						// functions should be treated as operators
						operators.push(str);
					} else {
						rpn.add(str);
					}
				} else if (Character.isDigit(c)) {
					String str = new String();
					while(Character.isDigit(c)) {
						str += c;
						i = sr.read();
						c = (char)i;
					}
					if (c == '.') {
						i = sr.read(); c = (char)i;
						str += '.';
						while(Character.isDigit(c)) {
							str += c;
							i = sr.read();
							c = (char)i;
						}
					}
					rpn.add(Double.parseDouble(str));
				} else if (c == ',') {
					while(!operators.empty()) {
						Object peekop = operators.peek();
						if (peekop instanceof Character) {
							if ((Character)peekop == '(' || (Character)peekop == ',') {
								operators.pop();
							        break;
							} else {
								rpn.add(operators.pop());
							}
						} else {
							rpn.add(operators.pop());
						}
					}
					operators.push(c);
					i = sr.read();
				} else if (c == '(') {
				        operators.push(c);
					i = sr.read();
				} else if (c == ')') {
				        while (!operators.empty()) {
						Object peekop = operators.peek();
						if (peekop instanceof Character) {
							if ((Character)peekop == '(' || (Character)peekop == ',') {
								operators.pop();
								break;
							} else {
								rpn.add(operators.pop());
							}
						} else {
							rpn.add(operators.pop());
						}
					}
					i = sr.read();
				} else if (Character.isWhitespace(c)) {
					i = sr.read();
				} else {
					
					int opPri = operatorPri(c);
					
					while (!operators.empty()) {

						boolean isrightassoc = isRightAssoc(c);
						int peekpri = 0;

						Object peekop = operators.peek();
						if (peekop instanceof Character) {
							char op = (Character)peekop;
							if (op == ',') {
								break;
							}
							peekpri = operatorPri(op);
						} else if (peekop instanceof String) {
							peekpri = functionPri;
						}

						if ((!isrightassoc && opPri >= peekpri) || (isrightassoc && opPri > peekpri)) {
							rpn.add(operators.pop());
						} else {
							break;
						}
						
					}

					operators.push(c);
					i = sr.read();
				}
			}
			while(!operators.empty()) {
				rpn.add(operators.pop());
			}
			
		} catch(IOException e) {
			System.out.println("Some error occured reading string: " + e.toString());
		} catch (NumberFormatException e) {
			System.out.println("Error when formatting number: " + e);
		}
	}
}
