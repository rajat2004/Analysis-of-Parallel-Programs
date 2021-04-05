package visitor;

import syntaxtree.*;
import utils.*;

import java.util.*;

/**
 * Perform inter-procedural, flow-insensitive, context-insensitive may alias analysis
 */
public class AliasAnalyzer<R,A> extends GJDepthFirst<R,A> {
   // These are accessed by the Main class

   // To check if analysis should be run again
   public boolean maps_updated = true;
   // When no more updates occur, Main will set this
   public boolean answer_alias_queries = false;

   // For printing debug statements
   // Use System.out.println() for actual output
   public static boolean debug = true;

   private void print(String s) {
      if (debug)
         System.out.println(this.getClass().getSimpleName() + ": " + s);
   }

   // Assign different object for each "new ()" statement by incrementing
   private int reference_count = 0;
   // Distinguish between Copy,Alloc & Load statements
   private boolean is_assignment = false;
   // Used for x = new A().foo() type statements
   private boolean is_message_send = false;
   private String left_var_name = null;


   // refs x fields -> values
   // Implemented as [ref][field] = value
   private HeapMap heap_map = new HeapMap();

   // Queue to store methods to be analysed
   private LinkedList<WorklistItem> worklist = new LinkedList<>();

   String curr_class = null;
   String curr_method = null;

   // Fields set in constructor
   SymbolTable st;

   public AliasAnalyzer(SymbolTable st_val) {
      super();
      st = st_val;
   }

   // Some helper methods

   private void updateMapsChanged(boolean changed) {
      if (changed && !maps_updated)
         maps_updated = true;
   }

   private ClassInfo getCurrClassInfo() {
      return st.getClassInfo(curr_class);
   }

   private FunctionSummary getCurrFuncSummary() {
      return getCurrClassInfo().getMethod(curr_method);
   }

   private StackMap getCurrentStack() {
      return getCurrFuncSummary().stack;
   }

   private void printStack() {
      print(curr_class + "::" + curr_method + " Stack");
      getCurrentStack().printAll();
   }

   //
   // Auto class visitors--probably don't need to be overridden.
   //
   public R visit(NodeList n, A argu) {
      R _ret=null;
      int _count=0;
      for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
         e.nextElement().accept(this,argu);
         _count++;
      }
      return _ret;
   }

   public R visit(NodeListOptional n, A argu) {
      if ( n.present() ) {
         R _ret=null;
         int _count=0;
         for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
            e.nextElement().accept(this,argu);
            _count++;
         }
         return _ret;
      }
      else
         return null;
   }

   public R visit(NodeOptional n, A argu) {
      if ( n.present() )
         return n.node.accept(this,argu);
      else
         return null;
   }

   public R visit(NodeSequence n, A argu) {
      R _ret=null;
      int _count=0;
      for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
         e.nextElement().accept(this,argu);
         _count++;
      }
      return _ret;
   }

   public R visit(NodeToken n, A argu) { return null; }

   //
   // User-generated visitor methods below
   //

   /**
    * f0 -> MainClass()
    * f1 -> ( TypeDeclaration() )*
    * f2 -> <EOF>
    */
   public R visit(Goal n, A argu) {
      R _ret=null;
      maps_updated = false;
      reference_count = 0;

      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);

      heap_map.printAll();
      return _ret;
   }

   /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> "public"
    * f4 -> "static"
    * f5 -> "void"
    * f6 -> "main"
    * f7 -> "("
    * f8 -> "String"
    * f9 -> "["
    * f10 -> "]"
    * f11 -> Identifier()
    * f12 -> ")"
    * f13 -> "{"
    * f14 -> ( VarDeclaration() )*
    * f15 -> ( QStatement() )*
    * f16 -> "}"
    * f17 -> "}"
    */
   public R visit(MainClass n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      String main_class = n.f1.f0.tokenImage;

      curr_class = main_class;

      n.f2.accept(this, argu);
      n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      n.f5.accept(this, argu);
      n.f6.accept(this, argu);

      curr_method = "main";

      n.f7.accept(this, argu);
      n.f8.accept(this, argu);
      n.f9.accept(this, argu);
      n.f10.accept(this, argu);
      n.f11.accept(this, argu);
      n.f12.accept(this, argu);
      n.f13.accept(this, argu);
      n.f14.accept(this, argu);
      n.f15.accept(this, argu);
      n.f16.accept(this, argu);
      n.f17.accept(this, argu);

      printStack();

      curr_method = null;
      curr_class = null;

      return _ret;
   }

   /**
    * f0 -> ClassDeclaration()
    *       | ClassExtendsDeclaration()
    */
   public R visit(TypeDeclaration n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> ( VarDeclaration() )*
    * f4 -> ( MethodDeclaration() )*
    * f5 -> "}"
    */
   public R visit(ClassDeclaration n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      curr_class = (String) n.f1.accept(this, argu);
      print("Current class: " + curr_class);
      n.f2.accept(this, argu);
      n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      n.f5.accept(this, argu);

      curr_class = null;
      return _ret;
   }

   /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "extends"
    * f3 -> Identifier()
    * f4 -> "{"
    * f5 -> ( VarDeclaration() )*
    * f6 -> ( MethodDeclaration() )*
    * f7 -> "}"
    */
   public R visit(ClassExtendsDeclaration n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      curr_class = (String) n.f1.accept(this, argu);
      String parent = (String) n.f2.accept(this, argu);

      print("Current class: " + curr_class + ", extends " + parent);

      n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      n.f5.accept(this, argu);
      n.f6.accept(this, argu);
      n.f7.accept(this, argu);

      curr_class = null;
      return _ret;
   }

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
   public R visit(VarDeclaration n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "public"
    * f1 -> Type()
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( FormalParameterList() )?
    * f5 -> ")"
    * f6 -> "{"
    * f7 -> ( VarDeclaration() )*
    * f8 -> ( QStatement() )*
    * f9 -> "return"
    * f10 -> Identifier()
    * f11 -> ";"
    * f12 -> "}"
    */
   public R visit(MethodDeclaration n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      String type = (String) n.f1.accept(this, argu);
      curr_method = (String) n.f2.accept(this, argu);

      print(curr_class + "::" + curr_method + " --- Return: " + type);

      if (!isMethodPresentInWorklist(curr_class, curr_method))
         return _ret;

      boolean erased = false;
      for(Iterator<WorklistItem> itr = worklist.iterator(); itr.hasNext(); ) {
         WorklistItem item = itr.next();
         if (item.isSame(curr_class, curr_method)) {
            print("Removing " + curr_class + ":::" + curr_method + " from worklist");
            itr.remove();
            erased = true;
         }
      }

      assert erased;

      n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      n.f5.accept(this, argu);
      n.f6.accept(this, argu);
      n.f7.accept(this, argu);
      n.f8.accept(this, argu);
      n.f9.accept(this, argu);

      String return_var = n.f10.f0.tokenImage;
      VarScope scope = isVarPresentInScope(return_var);

      print(curr_class + ":::" + curr_method + " Return: " + return_var + " ===> " + scope);
      if (scope.isPresent()) {
         boolean changed = getCurrFuncSummary().return_set.union(
                                                   getValuesFromScope(return_var, scope));
         updateMapsChanged(changed);
      }

      n.f11.accept(this, argu);
      n.f12.accept(this, argu);

      printStack();

      curr_method = null;
      return _ret;
   }

   /**
    * f0 -> FormalParameter()
    * f1 -> ( FormalParameterRest() )*
    */
   public R visit(FormalParameterList n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    */
   public R visit(FormalParameter n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> ","
    * f1 -> FormalParameter()
    */
   public R visit(FormalParameterRest n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> ArrayType()
    *       | BooleanType()
    *       | IntegerType()
    *       | Identifier()
    */
   public R visit(Type n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "int"
    * f1 -> "["
    * f2 -> "]"
    */
   public R visit(ArrayType n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "boolean"
    */
   public R visit(BooleanType n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "int"
    */
   public R visit(IntegerType n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> ( Query() )*
    * f1 -> Statement()
    */
   public R visit(QStatement n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> <SCOMMENT1>
    * f1 -> Identifier()
    * f2 -> "alias?"
    * f3 -> Identifier()
    * f4 -> <SCOMMENT2>
    */
   public R visit(Query n, A argu) {
      R _ret=null;
      if (!answer_alias_queries)
         return _ret;

      n.f0.accept(this, argu);
      String var1 = n.f1.f0.tokenImage;
      n.f2.accept(this, argu);
      String var2 = n.f3.f0.tokenImage;
      n.f4.accept(this, argu);

      print("Query: " + var1 + " " + var2);
      // Queries can be fields, params or stack variables
      ValuesSet vs1 = getValuesForVar(var1);
      ValuesSet vs2 = getValuesForVar(var2);

      boolean result = vs1.intersection(vs2);

      if (result)
         System.out.println("YES");
      else
         System.out.println("NO");

      return _ret;
   }

   /**
    * f0 -> Block()
    *       | AssignmentStatement()
    *       | ArrayAssignmentStatement()
    *       | FieldAssignmentStatement()
    *       | IfStatement()
    *       | WhileStatement()
    *       | PrintStatement()
    */
   public R visit(Statement n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "{"
    * f1 -> ( QStatement() )*
    * f2 -> "}"
    */
   public R visit(Block n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> Identifier()
    * f1 -> "="
    * f2 -> Expression()
    * f3 -> ";"
    */
   public R visit(AssignmentStatement n, A argu) {
      // Alloc, Copy or Load depending on Expression
      R _ret=null;
      String left_var = n.f0.f0.tokenImage;

      // FieldRead is painful to do here, and becomes pretty complicated
      is_assignment = true;
      left_var_name = left_var;

      print("Assignment: " + left_var);

      n.f1.accept(this, argu);
      ValuesSet right_values = (ValuesSet) n.f2.accept(this, argu);
      n.f3.accept(this, argu);

      // Field Assignment inside class method will need some special handling with *this* ptr
      VarScope left_var_scope = isVarPresentInScope(left_var);
      print("Assignment: " + left_var + " = ... ; " + left_var_scope);
      if (right_values!=null && left_var_scope.isPresent()) {

         if (left_var_scope.is_class_field) {
            print("Left var is a field, ah shit");
            // Requires updating all [this x field] ValueSets
            print("This refs: " + getCurrFuncSummary().this_ref);

            for(ReferenceObj ref : getCurrFuncSummary().this_ref) {
               ValuesSet left_values = heap_map.get(ref.toString(), left_var);
               boolean changed = left_values.union(right_values);
               updateMapsChanged(changed);

               // Case of `F = new A()`
               addAllFieldsToHeap(left_values);
            }
         }
         else {
            ValuesSet left_values = getValuesFromScope(left_var, left_var_scope);
            boolean changed = left_values.union(right_values);
            updateMapsChanged(changed);

            // Used for `x = new A()` case to populate heap
            addAllFieldsToHeap(left_values);
         }
      }

      is_assignment = false;
      left_var_name = null;

      return _ret;
   }

   private VarScope isVarPresentInScope(String var_name) {
      VarScope scope = new VarScope();
      // 1. Check local stack
      if (getCurrentStack().isVarPresent(var_name)) {
         scope.is_stack_var = true;
      }
      // 2. Parameters
      else if (getCurrFuncSummary().isFunctionParameter(var_name)) {
         scope.is_parameter = true;
      }
      // 3. Fields
      else if (getCurrClassInfo().isField(var_name)) {
         scope.is_class_field = true;
      }

      return scope;
   }

   private ValuesSet getValuesFromScope(String var_name, VarScope scope) {
      if (scope.is_stack_var)
         return getCurrentStack().getValues(var_name);
      else if (scope.is_parameter)
         return getCurrFuncSummary().getParameterValues(var_name);
      else if (scope.is_class_field) {
         return getAllValuesForField(var_name);
      }
      else
         return new ValuesSet();
   }

   // When too lazy to write 3 lines again ;-)
   private ValuesSet getValuesForVar(String var_name) {
      VarScope scope = isVarPresentInScope(var_name);
      ValuesSet values = getValuesFromScope(var_name, scope);
      print(var_name + " => " + scope + " ; " + values);
      return values;
   }

   private ValuesSet getAllValuesForField(String field) {
      // Class field needs union of all [this x field] ValueSets
      ValuesSet values = new ValuesSet();
      for(ReferenceObj ref : getCurrFuncSummary().this_ref) {
         values.union(heap_map.get(ref.toString(), field));
      }

      return values;
   }

   // Creates entries for all the references x fields in the heap
   private void addAllFieldsToHeap(ValuesSet values) {
      print("Adding Heap Field entries: " + values);
      for(ReferenceObj ref : values) {
         HashSet<String> all_fields = st.getClassInfo(ref.type).fields;
         print("Ref: " + ref + " ; Fields: " + all_fields);
         heap_map.createEntries(ref.toString(), all_fields);
      }
   }

   /**
    * f0 -> Identifier()
    * f1 -> "["
    * f2 -> Identifier()
    * f3 -> "]"
    * f4 -> "="
    * f5 -> Identifier()
    * f6 -> ";"
    */
   public R visit(ArrayAssignmentStatement n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      n.f5.accept(this, argu);
      n.f6.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> Identifier()
    * f1 -> "."
    * f2 -> Identifier()
    * f3 -> "="
    * f4 -> Identifier()
    * f5 -> ";"
    */
   public R visit(FieldAssignmentStatement n, A argu) {
      // Store: x.f = y
      R _ret=null;
      String left_obj_name = n.f0.f0.tokenImage;
      String field = n.f2.f0.tokenImage;
      String right_var = n.f4.f0.tokenImage;

      print("FieldAssignment: " + left_obj_name + "." + field + " = " + right_var);

      VarScope left_obj_scope = isVarPresentInScope(left_obj_name);
      ValuesSet left_values = getValuesFromScope(left_obj_name, left_obj_scope);

      print(left_obj_name + " => " + left_obj_scope + " ; Values: " + left_values);

      VarScope right_var_scope = isVarPresentInScope(right_var);
      ValuesSet right_values = getValuesFromScope(right_var, right_var_scope);
      print(right_var + " => " + right_var_scope + " ; Values: " + right_values);

      for(ReferenceObj ref : left_values) {
         boolean changed = heap_map.store(ref.toString(), field, right_values);
         updateMapsChanged(changed);
      }

      return _ret;
   }

   /**
    * f0 -> "if"
    * f1 -> "("
    * f2 -> Identifier()
    * f3 -> ")"
    * f4 -> Statement()
    * f5 -> "else"
    * f6 -> Statement()
    */
   public R visit(IfStatement n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      n.f5.accept(this, argu);
      n.f6.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "while"
    * f1 -> "("
    * f2 -> Identifier()
    * f3 -> ")"
    * f4 -> Statement()
    */
   public R visit(WhileStatement n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "System.out.println"
    * f1 -> "("
    * f2 -> Identifier()
    * f3 -> ")"
    * f4 -> ";"
    */
   public R visit(PrintStatement n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> AndExpression()
    *       | CompareExpression()
    *       | PlusExpression()
    *       | MinusExpression()
    *       | TimesExpression()
    *       | ArrayLookup()
    *       | ArrayLength()
    *       | MessageSend()
    *       | FieldRead()
    *       | PrimaryExpression()
    */
   public R visit(Expression n, A argu) {
      // FieldRead -> Load
      // Others are Primary expression
      R _ret=null;
      _ret = n.f0.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> Identifier()
    * f1 -> "&&"
    * f2 -> Identifier()
    */
   public R visit(AndExpression n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> Identifier()
    * f1 -> "<"
    * f2 -> Identifier()
    */
   public R visit(CompareExpression n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> Identifier()
    * f1 -> "+"
    * f2 -> Identifier()
    */
   public R visit(PlusExpression n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> Identifier()
    * f1 -> "-"
    * f2 -> Identifier()
    */
   public R visit(MinusExpression n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> Identifier()
    * f1 -> "*"
    * f2 -> Identifier()
    */
   public R visit(TimesExpression n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> Identifier()
    * f1 -> "["
    * f2 -> Identifier()
    * f3 -> "]"
    */
   public R visit(ArrayLookup n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      n.f3.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> Identifier()
    * f1 -> "."
    * f2 -> "length"
    */
   public R visit(ArrayLength n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> Identifier()
    * f1 -> "."
    * f2 -> Identifier()
    */
   public R visit(FieldRead n, A argu) {
      // Load: x = y.f
      R _ret=null;
      String right_obj_name = n.f0.f0.tokenImage;
      n.f1.accept(this, argu);
      String field = n.f2.f0.tokenImage;

      print("FieldRead: " + left_var_name + " = " + right_obj_name + "." + field);

      if (is_assignment && left_var_name!=null) {
         ValuesSet left_values = getValuesForVar(left_var_name);
         ValuesSet right_obj_refs = getValuesForVar(right_obj_name);

         for(ReferenceObj ref : right_obj_refs) {
            boolean changed = left_values.union(heap_map.get(ref.toString(), field));
            updateMapsChanged(changed);
         }
      }

      return _ret;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( ArgList() )?
    * f5 -> ")"
    */
   public R visit(MessageSend n, A argu) {
      R _ret=null;
      is_message_send = true;

      // This is needed since `new A()` can also create a temporary variable
      String var_name = (String) n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String method_name = n.f2.f0.tokenImage;

      print("MessageSend: " + var_name + "." + method_name + "()");

      ValuesSet refs = getValuesForVar(var_name);

      // Add methods to queue based on each possible ref type
      updateWorklist(refs, method_name);

      n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      n.f5.accept(this, argu);

      is_message_send = false;
      return _ret;
   }

   private void updateWorklist(ValuesSet refs, String method_name) {
      print("Previous Worklist: " + worklist);
      print("Updating Worklist: " + refs + ", Method: " + method_name);
      for(ReferenceObj ref : refs) {
         String type = ref.type;
         FunctionSummary method_info = st.getClassInfo(type).getMethod(method_name);

         // 1. Update this refs
         boolean changed = method_info.updateThisRefs(ref);
         updateMapsChanged(changed);

         // 2. Update return values
         // Only works since MessageSend requires a return value
         // Check if var present for primitive types
         VarScope left_var_scope = isVarPresentInScope(left_var_name);
         if (left_var_scope.isPresent()) {
            changed = getValuesFromScope(left_var_name, left_var_scope).union(method_info.return_set);
            updateMapsChanged(changed);
         }

         // 3. Update the function parameter values
         // TODO!!!

         if (!isMethodPresentInWorklist(type, method_name)) {
            WorklistItem item = new WorklistItem(type, method_name);
            print("Not present, Adding " + item);
            worklist.add(item);
         }
      }

      print("New Worklist: " + worklist);
   }

   private boolean isMethodPresentInWorklist(String cname, String method_name) {
      for(WorklistItem item : worklist) {
         if (item.isSame(cname, method_name))
            return true;
      }

      return false;
   }

   /**
    * f0 -> Identifier()
    * f1 -> ( ArgRest() )*
    */
   public R visit(ArgList n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> ","
    * f1 -> Identifier()
    */
   public R visit(ArgRest n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> IntegerLiteral()
    *       | TrueLiteral()
    *       | FalseLiteral()
    *       | Identifier()
    *       | ThisExpression()
    *       | ArrayAllocationExpression()
    *       | AllocationExpression()
    *       | NotExpression()
    */
   public R visit(PrimaryExpression n, A argu) {
      R _ret=null;
      _ret = n.f0.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> <INTEGER_LITERAL>
    */
   public R visit(IntegerLiteral n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "true"
    */
   public R visit(TrueLiteral n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "false"
    */
   public R visit(FalseLiteral n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> <IDENTIFIER>
    */
   public R visit(Identifier n, A argu) {
      n.f0.accept(this, argu);
      String name = n.f0.tokenImage;
//      print("Identifier: " + name);

      // Handle the basic case => x = y
      // Screwed up implementation due to ValuesSet being used as the return
      if (is_assignment && !is_message_send) {
         VarScope scope = isVarPresentInScope(name);
         print("Assignment Identifier: " + name + ": " + scope);
         if (scope.isPresent())
            return (R)getValuesFromScope(name, scope);
         else
            return (R)(new ValuesSet());
      }

      return (R)name;
   }

   /**
    * f0 -> "this"
    */
   public R visit(ThisExpression n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "new"
    * f1 -> "int"
    * f2 -> "["
    * f3 -> Identifier()
    * f4 -> "]"
    */
   public R visit(ArrayAllocationExpression n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"
    */
   public R visit(AllocationExpression n, A argu) {
//      R _ret=null;
      n.f0.accept(this, argu);
      String type = n.f1.f0.tokenImage;
      n.f2.accept(this, argu);
      n.f3.accept(this, argu);

      reference_count++;
      ReferenceObj ref = new ReferenceObj(type, reference_count);

      if (is_message_send) {
         // Expects a variable name
         // Generate a tmp variable for each new alloc, this simplifies the code
         // 1. x = new A().foo()
         //    Treat it as a normal y.foo() call
         String tmp_var = "TMP" + reference_count;
         // This is normally done in SymbolTableGen
         getCurrentStack().add(tmp_var);

         getCurrentStack().getValues(tmp_var).add(ref);
         addAllFieldsToHeap(getCurrentStack().getValues(tmp_var));

         print("MessageSend: Tmp Var: " + tmp_var);
         return (R)tmp_var;
      }
      else {
         // Expects a ValuesSet
         print("New ref: " + ref.toString());
         ValuesSet tmp_vs = new ValuesSet();
         tmp_vs.add(ref);
         return (R)tmp_vs;
      }
   }

   /**
    * f0 -> "!"
    * f1 -> Identifier()
    */
   public R visit(NotExpression n, A argu) {
      R _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      return _ret;
   }

}
