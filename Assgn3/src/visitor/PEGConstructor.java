//
// Generated by JTB 1.3.2
//

package visitor;
import syntaxtree.*;
import utils.PEGNode;
import utils.PEGNodeType;
import utils.ParallelExecutionGraph;
import utils.SymbolTable;

import java.util.*;

/**
 * Provides default methods which visit each node in the tree in depth-first
 * order.  Your visitors may extend this class.
 */
public class PEGConstructor<R,A> extends GJDepthFirst<R,A> {
    public static boolean debug = true;

    public void print(String s) {
        if (debug)
            System.out.println(this.getClass().getSimpleName() + ": " + s);
    }

    public ParallelExecutionGraph peg = new ParallelExecutionGraph();
    public boolean next_iteration = true;

    private static int global_node_id = 0;
    private HashMap<String, Iterator<String>> current_threads = new HashMap<>();
    private String curr_class = null;
    private String curr_thread = null;
    private String curr_label = null;
    private HashSet<String> sync_objs = new HashSet<>();

    // Fields set in constructor
    SymbolTable st;

    public PEGConstructor(SymbolTable st) {
        super();
        this.st = st;

        print("Populating PEG with each thread");
        st.thread_objects.forEach((class_name, threads) -> {
            threads.forEach(thread_id -> {
                peg.addThreadObject(thread_id, class_name);
            });

            current_threads.put(class_name, threads.iterator());
        });
    }

    private PEGNode createNode(String obj, PEGNodeType type) {
        global_node_id++;
        // Node can only be created for the current thread and current label
        PEGNode node = new PEGNode(obj, type, curr_thread, global_node_id, curr_label);

        // No direct assign, copy each String individually
        node.sync_objs.addAll(sync_objs);

        print("Created new node: " + node);
        // Reset label so that no one can use it again
        // TODO: Confirm that a label only applies to one node!!!
        curr_label = null;
        return node;
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
     * f2 -> ( Query() )*
     * f3 -> <EOF>
     */
    public R visit(Goal n, A argu) {
        R _ret=null;

        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);

        next_iteration = false;

        current_threads.forEach((class_name, thread_iter) -> {
            if (thread_iter.hasNext()) {
                print("Next iteration required for class: " + class_name);
                next_iteration = true;
            }
        });

        if (!next_iteration) {
            print("Completed PEG construction!!!");
            print("Now starting PEG initial nodes addition!");
            peg.createInitialEdges();
            peg.verifyPEG();
        }

        peg.printAll();

        return _ret;
    }

    private boolean updateCurrentThread() {
        if (current_threads.get(curr_class).hasNext()) {
            curr_thread = current_threads.get(curr_class).next();
            print("New current thread: " + curr_thread);
            return true;
        }
        else {
            print("No more threads remaining for class: " + curr_class);
            return false;
        }
    }

    private PEGNode createAddNodeToPEG(String obj, PEGNodeType type) {
        PEGNode node = createNode(obj, type);
        peg.addNodeToThread(curr_thread, node);
        return node;
    }

    private void startThreadCFG() {
        print("Starting CFG for thread: " + curr_thread);
        createAddNodeToPEG("*", PEGNodeType.THREAD_BEGIN);
    }

    private void endThreadCFG() {
        createAddNodeToPEG("*", PEGNodeType.THREAD_END);
        print("Completed CFG of thread: " + curr_thread + "\n\n");

        curr_thread = null;
        curr_class = null;
        curr_label = null;
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
     * f14 -> "try"
     * f15 -> "{"
     * f16 -> ( VarDeclaration() )*
     * f17 -> ( QParStatement() )*
     * f18 -> "}"
     * f19 -> "catch"
     * f20 -> "("
     * f21 -> Identifier()
     * f22 -> Identifier()
     * f23 -> ")"
     * f24 -> "{"
     * f25 -> "}"
     * f26 -> "}"
     * f27 -> "}"
     */
    public R visit(MainClass n, A argu) {
        R _ret=null;
        n.f0.accept(this, argu);
        curr_class = n.f1.f0.tokenImage;
        print("Starting main class: " + curr_class);

        if (!updateCurrentThread()) {
            // No need to analyze class which doesn't have any more threads
            return _ret;
        }

        startThreadCFG();

        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
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
        n.f18.accept(this, argu);
        n.f19.accept(this, argu);
        n.f20.accept(this, argu);
        n.f21.accept(this, argu);
        n.f22.accept(this, argu);
        n.f23.accept(this, argu);
        n.f24.accept(this, argu);
        n.f25.accept(this, argu);
        n.f26.accept(this, argu);
        n.f27.accept(this, argu);

        endThreadCFG();

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
     * f4 -> "}"
     */
    public R visit(ClassDeclaration n, A argu) {
        R _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> "Thread"
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )*
     * f6 -> ( MethodDeclaration() )*
     * f7 -> "}"
     */
    public R visit(ClassExtendsDeclaration n, A argu) {
        R _ret=null;
        n.f0.accept(this, argu);
        curr_class = n.f1.f0.tokenImage;
        print("Starting class: " + curr_class);
        if (!updateCurrentThread()) {
            // No need to analyze class which doesn't have any more threads
            return _ret;
        }

        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        n.f7.accept(this, argu);
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
     * f1 -> "void"
     * f2 -> "run"
     * f3 -> "("
     * f4 -> ")"
     * f5 -> "{"
     * f6 -> "try"
     * f7 -> "{"
     * f8 -> ( VarDeclaration() )*
     * f9 -> ( QParStatement() )*
     * f10 -> "}"
     * f11 -> "catch"
     * f12 -> "("
     * f13 -> Identifier()
     * f14 -> Identifier()
     * f15 -> ")"
     * f16 -> "{"
     * f17 -> "}"
     * f18 -> "}"
     */
    public R visit(MethodDeclaration n, A argu) {
        R _ret=null;

        startThreadCFG();

        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
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
        n.f18.accept(this, argu);

        endThreadCFG();

        return _ret;
    }

    /**
     * f0 -> BooleanType()
     *       | IntegerType()
     *       | Identifier()
     */
    public R visit(Type n, A argu) {
        R _ret=null;
        n.f0.accept(this, argu);
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
     * f0 -> ( Ann() )*
     * f1 -> Statement()
     */
    public R visit(QParStatement n, A argu) {
        R _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> <SCOMMENT1>
     * f1 -> Label()
     * f2 -> <SCOMMENT2>
     */
    public R visit(Ann n, A argu) {
        R _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> Identifier()
     * f1 -> ":"
     */
    public R visit(Label n, A argu) {
        R _ret=null;
        curr_label = n.f0.f0.tokenImage;
        n.f1.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> Block()
     *       | AssignmentStatement()
     *       | FieldAssignmentStatement()
     *       | IfStatement()
     *       | WhileStatement()
     *       | MessageSend()
     *       | PrintStatement()
     *       | SynchStatement()
     */
    public R visit(Statement n, A argu) {
        R _ret=null;
        n.f0.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "{"
     * f1 -> ( QParStatement() )*
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
        R _ret=null;
        String left_var = n.f0.f0.tokenImage;
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        print("\nAssignment: " + left_var + " = ...");
        createAddNodeToPEG("*", PEGNodeType.NORMAL);
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
        R _ret=null;
        String left_obj = n.f0.f0.tokenImage;
        n.f1.accept(this, argu);
        String field = n.f2.f0.tokenImage;
        n.f3.accept(this, argu);
        String right_var = n.f4.f0.tokenImage;
        n.f5.accept(this, argu);
        print("\nFieldAssignmentStatement: " + left_obj + "." + field + " = " + right_var);

        createAddNodeToPEG("*", PEGNodeType.NORMAL);
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
//        n.f2.accept(this, argu);
        String check_var = n.f2.f0.tokenImage;

        print("If condition: " + check_var);
        PEGNode if_node = createAddNodeToPEG("*", PEGNodeType.IF);

        n.f3.accept(this, argu);
        n.f4.accept(this, argu);

        PEGNode if_end_node = createAddNodeToPEG("*", PEGNodeType.IF_END);

        n.f5.accept(this, argu);

        print("Else condition for " + check_var);
        PEGNode else_node = createAddNodeToPEG("*", PEGNodeType.ELSE);

        n.f6.accept(this, argu);

        PEGNode if_else_end_node = createAddNodeToPEG("*", PEGNodeType.IF_ELSE_END);
        print("Added if-else-end node: " + if_else_end_node);

        // If -> if block
        //       else block
        if_node.local_successors.add(else_node);
        else_node.local_predecessors.add(if_node);

        // End of if block -> next after else block
        if_end_node.local_successors.add(if_else_end_node);
        if_else_end_node.local_predecessors.add(if_end_node);

        // Removal of if_end -> else is handled in ThreadInfo

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

        String check_var = n.f2.f0.tokenImage;
        print("While: " + check_var);
        PEGNode while_node = createAddNodeToPEG("*", PEGNodeType.WHILE);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);

        PEGNode while_jump_node = createAddNodeToPEG("*", PEGNodeType.WHILE_JUMP);
        PEGNode while_end_node = createAddNodeToPEG("*", PEGNodeType.WHILE_END);

        // While -> While block
        //       -> After while block
        while_node.local_successors.add(while_end_node);
        while_end_node.local_predecessors.add(while_node);

        while_jump_node.local_successors.add(while_node);
        while_node.local_predecessors.add(while_jump_node);

        // No connection between While_jump and While_end is handled in ThreadInfo

        return _ret;
    }

    /**
     * f0 -> "synchronized"
     * f1 -> "("
     * f2 -> Identifier()
     * f3 -> ")"
     * f4 -> Statement()
     */
    public R visit(SynchStatement n, A argu) {
        R _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        String obj_name = n.f2.f0.tokenImage;
        n.f3.accept(this, argu);

        print("Synchronized Entry: " + obj_name);
        createAddNodeToPEG(obj_name, PEGNodeType.SYNC_ENTRY);

        // sync entry node isn't synchronized
        sync_objs.add(obj_name);

        n.f4.accept(this, argu);

        print("Synchronized Exit: " + obj_name);
        createAddNodeToPEG(obj_name, PEGNodeType.SYNC_EXIT);

        // sync exit is synchronized
        sync_objs.remove(obj_name);

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
        print("Print statement");
        createAddNodeToPEG("*", PEGNodeType.NORMAL);
        return _ret;
    }

    /**
     * f0 -> AndExpression()
     *       | CompareExpression()
     *       | PlusExpression()
     *       | MinusExpression()
     *       | TimesExpression()
     *       | FieldRead()
     *       | PrimaryExpression()
     */
    public R visit(Expression n, A argu) {
        R _ret=null;
        n.f0.accept(this, argu);
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
     * f1 -> "."
     * f2 -> Identifier()
     */
    public R visit(FieldRead n, A argu) {
        R _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> callStartMethod()
     *       | callNotifyMethod()
     *       | callNotifyAllMethod()
     *       | callWaitMethod()
     *       | callJoinMethod()
     */
    public R visit(MessageSend n, A argu) {
        R _ret=null;
        n.f0.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "."
     * f2 -> "start"
     * f3 -> "("
     * f4 -> ")"
     * f5 -> ";"
     */
    public R visit(callStartMethod n, A argu) {
        R _ret=null;
//        n.f0.accept(this, argu);
        String thread_id = n.f0.f0.tokenImage;
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);

        print("Starting thread: " + thread_id);
        createAddNodeToPEG(thread_id, PEGNodeType.THREAD_START);
        return _ret;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "."
     * f2 -> "notify"
     * f3 -> "("
     * f4 -> ")"
     * f5 -> ";"
     */
    public R visit(callNotifyMethod n, A argu) {
        R _ret=null;
//        n.f0.accept(this, argu);
        String obj_name = n.f0.f0.tokenImage;
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);

        print("Notify: " + obj_name);
        createAddNodeToPEG(obj_name, PEGNodeType.NOTIFY);
        return _ret;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "."
     * f2 -> "notifyAll"
     * f3 -> "("
     * f4 -> ")"
     * f5 -> ";"
     */
    public R visit(callNotifyAllMethod n, A argu) {
        R _ret=null;
//        n.f0.accept(this, argu);
        String obj_name = n.f0.f0.tokenImage;
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);

        print("NotifyAll: " + obj_name);
        createAddNodeToPEG(obj_name, PEGNodeType.NOTIFY_ALL);
        return _ret;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "."
     * f2 -> "wait"
     * f3 -> "("
     * f4 -> ")"
     * f5 -> ";"
     */
    public R visit(callWaitMethod n, A argu) {
        R _ret=null;
//        n.f0.accept(this, argu);
        String obj_name = n.f0.f0.tokenImage;
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);

        print("Wait: " + obj_name);
        // TODO: Check if label has to be applied on WAIT or WAITING, currently assuming WAIT

        // IF waiting node has label
//        String label = curr_label;
//        curr_label = null;

        PEGNode wait_node = createAddNodeToPEG(obj_name, PEGNodeType.WAIT);

        // Restore label for Waiting node
//        curr_label = label;

        PEGNode waiting_node = createAddNodeToPEG(obj_name, PEGNodeType.WAITING);
        waiting_node.sync_objs.remove(obj_name);

        PEGNode notified_entry_node = createAddNodeToPEG(obj_name, PEGNodeType.NOTIFIED_ENTRY);
        notified_entry_node.sync_objs.remove(obj_name);

        return _ret;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "."
     * f2 -> "join"
     * f3 -> "("
     * f4 -> ")"
     * f5 -> ";"
     */
    public R visit(callJoinMethod n, A argu) {
        R _ret=null;
//        n.f0.accept(this, argu);
        String thread_id = n.f0.f0.tokenImage;
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);

        print("Joining thread: " + thread_id);
        createAddNodeToPEG(thread_id, PEGNodeType.THREAD_JOIN);
        return _ret;
    }

    /**
     * f0 -> IntegerLiteral()
     *       | TrueLiteral()
     *       | FalseLiteral()
     *       | Identifier()
     *       | ThisExpression()
     *       | AllocationExpression()
     *       | NotExpression()
     */
    public R visit(PrimaryExpression n, A argu) {
        R _ret=null;
        n.f0.accept(this, argu);
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
        R _ret=null;
        n.f0.accept(this, argu);
        return (R)n.f0.tokenImage;
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
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    public R visit(AllocationExpression n, A argu) {
        R _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        return _ret;
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

    /**
     * f0 -> <SCOMMENT1>
     * f1 -> Identifier()
     * f2 -> "mhp?"
     * f3 -> Identifier()
     * f4 -> <SCOMMENT2>
     */
    public R visit(Query n, A argu) {
        R _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        return _ret;
    }

}
