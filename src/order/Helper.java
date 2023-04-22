package order;

import java.util.Arrays;

/**
 * Helper class of the client and the server.
 */
public class Helper {

    public static final String[] requestsTypes = new String[]{"GET", "PUT", "DELETE"};
    public static String logWithTimestamp(String message) {
        return String.format("%s %s", System.currentTimeMillis(), message);
    }

    public static String getOperationType(String operation) {
        if(!operation.contains("(")) {
            return "";
        }
        return operation.substring(0, operation.indexOf("("));
    }

    public static int[] getOperationParams(String operation) {
        if(!operation.contains("(") || !operation.contains(")")) {
            return new int[]{};
        }

        String params = operation.substring(operation.indexOf("(") + 1, operation.indexOf(")"));
        return Arrays.stream(params.split(",")).mapToInt(Integer::parseInt).toArray();
    }

    public static boolean isClientRequestValid(String operationType, int[] nums) {
        if (!Arrays.asList(Helper.requestsTypes).contains(operationType) || nums.length == 0
                || (operationType.equals("GET") && nums.length != 1)
                || (operationType.equals("DELETE") && nums.length != 1)
                || operationType.equals("PUT") && nums.length != 2) {
            return false;
        }
        return true;
    }

    public static boolean isServerResponseValid(String result, String operation) {
        String[] serverResponse = result.split("-");
        if (!serverResponse[0].equals(operation)) {
            return false;
        }
        return true;
    }
}
