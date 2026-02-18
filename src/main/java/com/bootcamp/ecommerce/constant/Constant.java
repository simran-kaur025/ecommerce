package com.bootcamp.ecommerce.constant;

public interface Constant {
    String SUCCESS = "SUCCESS";
    String FAIL = "FAIL";

    public static final String RESET_PASSWORD_SUBJECT = "Reset Password to Your Account";
    public static final String ACTIVATE_ACCOUNT_SUBJECT = "Activate Your Account";
    public static final String SELLER_CREATED_SUBJECT = "Seller Account Created - Awaiting Approval";

    public static final String PRODUCT_ACTIVATED_SUBJECT = "Product Activated Successfully";
    public static final String PRODUCT_DEACTIVATED_SUBJECT = "Product Deactivated";

    public static final String RESET_PASSWORD_URL =
            "http://localhost:8080/users/updatepassword?token=";

    public static final String ACTIVATE_ACCOUNT_URL =
            "http://localhost:8080/api/register/customer/activate?token=";

    public static final String SELLER_BODY = """
            Dear Seller,

            Your seller account has been successfully created.

            Our admin team is currently reviewing your account.
            You will be notified once your account is approved.

            Thank you for registering with us.

            Regards,
            Ecommerce Team
            """;




}
