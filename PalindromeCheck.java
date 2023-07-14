public class PalindromeCheck {

    public static void main(String args[]){
        boolean palindromeCheck = isPalindrome(args[0]);
        if (palindromeCheck){
            System.out.println("sequence " + args[0] + " is a palindrome");
        } else {
            System.out.println("sequence " + args[0] + " is not a palindrome");
        }
    }

    private static boolean isPalindrome(String sequence){
        boolean palindrome = false;
        try {
            // make sure input is parseable to a number
            long numberSequence = Long.valueOf(sequence);
            int sequenceLength = sequence.length();
            int sequenceHalved = sequenceLength / 2;
            int j = sequenceLength-1;
            for (int i = 0; i < sequenceHalved; i++){
                if (Character.compare(sequence.charAt(i), sequence.charAt(j)) == 0){
                    palindrome = true;
                    j--;
                }
                else {
                    palindrome = false;
                    break;
                }
            }
        } catch (NumberFormatException nfe){
            System.out.println("invalid input");
        } catch (IndexOutOfBoundsException ioobe){
            System.out.println("input sequence exhausted");
        }
        return palindrome;
    }
}