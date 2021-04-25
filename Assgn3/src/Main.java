import syntaxtree.*;
import visitor.*;

public class Main {
    public static void main(String[] args) {
        try {
            Node root = new QParJavaParser(System.in).Goal();
            System.out.println("Parsed successfully.");

            PEGConstructor peg_constructor = new PEGConstructor();
            root.accept(peg_constructor, null);

            MHPAnalyzer mhp_analyzer = new MHPAnalyzer();
            root.accept(mhp_analyzer, null);
            //GJDepthFirst v = new GJDepthFirst();
            //root.accept(v, null);
        } catch (ParseException e) {
            System.out.println(e.toString());
        }
    }
}
