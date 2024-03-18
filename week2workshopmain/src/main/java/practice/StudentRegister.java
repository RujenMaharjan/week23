package practice;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(asyncSupported = true, urlPatterns = { "/StudentRegister" })
public class StudentRegister extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public StudentRegister() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter printOut = response.getWriter();
        response.setContentType("text/html");
        printOut.print("Hello World!");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter printOut = response.getWriter();

        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String dobString = request.getParameter("birthday");
        LocalDate dob = LocalDate.parse(dobString);
        String gender = request.getParameter("gender");
        String email = request.getParameter("email");
        String phoneNumber = request.getParameter("phoneNumber");
        String subject = request.getParameter("subject");


        // Validation Criteria
        boolean isValid = true;
        StringBuilder errorMessage = new StringBuilder();

        // 1. Name Format Validation
        if (!Pattern.matches("[a-zA-Z]+", firstName) || !Pattern.matches("[a-zA-Z]+", lastName)) {
            isValid = false;
            errorMessage.append("First name and last name should not contain numbers or special characters.<br>");
        }

        // 2. Minimum Username Length Requirement
        if (firstName.length() < 6 || lastName.length() < 6) {
            isValid = false;
            errorMessage.append("First name and last name should have a minimum length of 6 characters.<br>");
        }

        // 3. Birthday Date Restriction
        LocalDate currentDate = LocalDate.now();
        if (dob.isAfter(currentDate)) {
            isValid = false;
            errorMessage.append("Invalid birthday. Please select a date earlier than the current date.<br>");
        }

        // 4. Phone Number Format Requirement
        if (!phoneNumber.startsWith("+") || phoneNumber.length() != 14) {
            isValid = false;
            errorMessage.append("Phone number should start with a '+' sign and have a length of 14 characters.<br>");
        }

        // 5. Password Complexity Requirement
        // Not applicable in this context

        // 6. Data Duplication Identification Requirement
        try {
            Class.forName("com.mysql.jdbc.Driver");

            String url = "jdbc:mysql://localhost:3306/c11";
            String user = "root";
            String pass = "";
            Connection con = DriverManager.getConnection(url, user, pass);

            // Check if username, email, or phone number already exists
            String query = "SELECT * FROM student_info WHERE first_name=? OR email=? OR phone_number=?";
            PreparedStatement checkStatement = con.prepareStatement(query);
            checkStatement.setString(1, firstName);
            checkStatement.setString(2, email);
            checkStatement.setString(3, phoneNumber);
            ResultSet resultSet = checkStatement.executeQuery();

            if (resultSet.next()) {
                isValid = false;
                errorMessage.append("Username, email, or phone number already exists.<br>");
            }

            // Proceed with registration if all validations pass
            if (isValid) {
                String insertQuery = "INSERT INTO student_info (first_name, last_name, dob, gender, email, phone_number, subject) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement st = con.prepareStatement(insertQuery);

                st.setString(1, firstName);
                st.setString(2, lastName);
                st.setDate(3, Date.valueOf(dob));
                st.setString(4, gender);
                st.setString(5, email);
                st.setString(6, phoneNumber);
                st.setString(7, subject);
                

                int result = st.executeUpdate();
                if (result > 0) {
                    printOut.println("<h1>Your account is registered as</h1>");
                    printOut.println("<h3>Name: " + firstName + "</h3>");
                    printOut.println("<h3>Gender: " + gender + "</h3>");
                } else {
                    printOut.println("<h1>Sorry! Your data is not registered.</h1>");
                }
            } else {
                printOut.println("<h1>Validation Error:</h1>");
                printOut.println(errorMessage.toString());
            }

        } catch (SQLException | ClassNotFoundException ex) {
            printOut.println("<h1>Please enter the correct data!</h1>");
            ex.printStackTrace();
        }
        printOut.println("<h1>End!</h1>");
    }
}
