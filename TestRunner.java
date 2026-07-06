import com.fooddelivery.user.domain.UserTest;
import com.fooddelivery.order.domain.OrderTest;
import com.fooddelivery.restaurant.domain.RestaurantTest;
import com.fooddelivery.delivery.domain.DeliveryTest;
import com.fooddelivery.notification.domain.NotificationTest;

public class TestRunner {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("STARTING FOOD DELIVERY PLATFORM TEST SUITE RUNNER");
        System.out.println("==================================================");

        int totalTests = 0;
        int passedTests = 0;

        // User Bounded Context Tests
        try {
            System.out.print("Running UserTest.testUserInitialization... ");
            new UserTest().testUserInitialization();
            System.out.println("PASSED");
            passedTests++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
        }
        totalTests++;

        try {
            System.out.print("Running UserTest.testUserActivation... ");
            new UserTest().testUserActivation();
            System.out.println("PASSED");
            passedTests++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
        }
        totalTests++;

        try {
            System.out.print("Running UserTest.testDeletedUserActivationFails... ");
            new UserTest().testDeletedUserActivationFails();
            System.out.println("PASSED");
            passedTests++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
        }
        totalTests++;

        // Order Bounded Context Tests
        try {
            System.out.print("Running OrderTest.testOrderInitialization... ");
            new OrderTest().testOrderInitialization();
            System.out.println("PASSED");
            passedTests++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
        }
        totalTests++;

        try {
            System.out.print("Running OrderTest.testOrderPaymentTransition... ");
            new OrderTest().testOrderPaymentTransition();
            System.out.println("PASSED");
            passedTests++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
        }
        totalTests++;

        try {
            System.out.print("Running OrderTest.testInvalidOrderTransitionFails... ");
            new OrderTest().testInvalidOrderTransitionFails();
            System.out.println("PASSED");
            passedTests++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
        }
        totalTests++;

        // Restaurant Bounded Context Tests
        try {
            System.out.print("Running RestaurantTest.testRestaurantInitialization... ");
            new RestaurantTest().testRestaurantInitialization();
            System.out.println("PASSED");
            passedTests++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
        }
        totalTests++;

        try {
            System.out.print("Running RestaurantTest.testRestaurantApproval... ");
            new RestaurantTest().testRestaurantApproval();
            System.out.println("PASSED");
            passedTests++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
        }
        totalTests++;

        try {
            System.out.print("Running RestaurantTest.testRestaurantRejection... ");
            new RestaurantTest().testRestaurantRejection();
            System.out.println("PASSED");
            passedTests++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
        }
        totalTests++;

        try {
            System.out.print("Running RestaurantTest.testRestaurantSuspension... ");
            new RestaurantTest().testRestaurantSuspension();
            System.out.println("PASSED");
            passedTests++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
        }
        totalTests++;

        try {
            System.out.print("Running RestaurantTest.testRestaurantSurgeMultiplier... ");
            new RestaurantTest().testRestaurantSurgeMultiplier();
            System.out.println("PASSED");
            passedTests++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
        }
        totalTests++;

        // Delivery Bounded Context Tests
        try {
            System.out.print("Running DeliveryTest.testDriverInitialization... ");
            new DeliveryTest().testDriverInitialization();
            System.out.println("PASSED");
            passedTests++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
        }
        totalTests++;

        try {
            System.out.print("Running DeliveryTest.testDeliveryAssignmentInitialization... ");
            new DeliveryTest().testDeliveryAssignmentInitialization();
            System.out.println("PASSED");
            passedTests++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
        }
        totalTests++;

        // Notification Bounded Context Tests
        try {
            System.out.print("Running NotificationTest.testNotificationInitialization... ");
            new NotificationTest().testNotificationInitialization();
            System.out.println("PASSED");
            passedTests++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
        }
        totalTests++;

        try {
            System.out.print("Running NotificationTest.testNotificationRendering... ");
            new NotificationTest().testNotificationRendering();
            System.out.println("PASSED");
            passedTests++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
        }
        totalTests++;

        try {
            System.out.print("Running NotificationTest.testNotificationRetryFailureAndSuccess... ");
            new NotificationTest().testNotificationRetryFailureAndSuccess();
            System.out.println("PASSED");
            passedTests++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
        }
        totalTests++;

        System.out.println("==================================================");
        System.out.printf("TEST RUN COMPLETE: %d/%d PASSED\n", passedTests, totalTests);
        System.out.println("==================================================");

        if (passedTests != totalTests) {
            System.exit(1);
        }
    }
}
