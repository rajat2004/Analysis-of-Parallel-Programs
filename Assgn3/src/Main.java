import syntaxtree.*;
import utils.Utils;
import visitor.*;

public class Main {
    public static void main(String[] args) {
        try {
            Node root = new QParJavaParser(System.in).Goal();
            print("Parsed successfully.");

            SymbolTableGenerator st_gen = new SymbolTableGenerator();
            for(int i=0; i<st_gen.n_iterations; i++) {
                print("SymbolTableGen: Iteration " + (i+1));
                root.accept(st_gen, null);
            }

            PEGConstructor peg_constructor = new PEGConstructor(st_gen.st);
            int peg_const_iter = 0;
            while (peg_constructor.next_iteration) {
                print("PEGConstructor: Iteration " + peg_const_iter);
                peg_const_iter++;
                root.accept(peg_constructor, null);
            }

            MHPAnalyzer mhp_analyzer = new MHPAnalyzer();
            root.accept(mhp_analyzer, null);
            //GJDepthFirst v = new GJDepthFirst();
            //root.accept(v, null);
        } catch (ParseException e) {
            System.out.println(e.toString());
        }
    }

    private static void print(String s) {
        Utils.print("\n\n\n" + s);
    }
}
