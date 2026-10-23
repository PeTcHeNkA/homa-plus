import java.util.Arrays;

public class TestMinS {


    public static void main(String[] args) {
        int[] arr = new int[]{1, 2, 3, 4, 5, 7, 8, 9, 10};

        System.out.println(binarySearch0(arr, 0, arr.length, 7));




    }




    private static int binarySearch0(int[] a, int fromIndex, int toIndex,
                                     int key) {
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            int midVal = a[mid];

            if (midVal < key)
                low = mid + 1;
            else if (midVal > key)
                high = mid - 1;
            else
                return mid; // key found
        }
        return low - 1; // key not found
    }
}