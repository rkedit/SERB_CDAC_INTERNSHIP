public class FunctionTester2 {
    public static void main(String[] args) {
        System.out.println("Sum using imperative way. Sum(5) : " + sum(5));
        System.out.println("Sum using recursive way. Sum(5) : " + sumRecursive(5));
    }

    private static int sum(int n){
        int result = 0;
        for(int i = 1; i <= n; i++){
            result = result + i;
        }
        return result;
    }

    private static int sumRecursive(int n){
        if(n == 1){
            return 1;
        }else{
            return n + sumRecursive(n-1);
        }
    }
}