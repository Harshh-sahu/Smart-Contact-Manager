# Smart Contact Manager

Smart Contact Manager is a robust web application designed to manage contacts efficiently. It leverages a suite of technologies including Thymeleaf, Spring Boot, JWT, Razorpay Payment Gateway, HTML, CSS, JavaScript, Hibernate, and MySQL. This README provides a detailed guide on setting up and starting the project.

## Project Overview

Smart Contact Manager offers a secure and user-friendly interface for managing contacts. It includes features such as authentication, payment integration, and advanced contact management functionalities.

## Technologies Used

- **Frontend**: Thymeleaf, HTML, CSS, JavaScript
- **Backend**: Spring Boot, JWT, Hibernate
- **Database**: MySQL
- **Payment Gateway**: Razorpay

## Prerequisites

Before you begin, ensure you have the following installed:

- Java Development Kit (JDK) 11 or higher
- Maven
- MySQL
- An IDE such as IntelliJ IDEA or Eclipse

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/smart-contact-manager.git
cd smart-contact-manager




2. Configure the Database

Create a MySQL database:



CREATE DATABASE smart_contact_manager;




  application.properties

spring.datasource.url=jdbc:mysql://localhost:3306/smart_contact_manager
spring.datasource.username=your-username
spring.datasource.password=your-password
spring.jpa.hibernate.ddl-auto=update


3. Install Dependencies


Navigate to the project directory and run the following command to install dependencies:


mvn clean install



4. Run the Application


Start the Spring Boot application:


mvn spring-boot:run


The application should now be running on http://localhost:8080.

Features
User Authentication: Secure login and registration using JWT.

Contact Management: Add, update, delete, and view contacts.

Payment Integration: Integrated with Razorpay for handling payments.

Responsive UI: Built with Thymeleaf, HTML, CSS, and JavaScript for a dynamic and responsive user interface.
Usage

Open your browser and navigate to http://localhost:8080.

Register a new account or log in with your existing credentials.

Use the dashboard to manage your contacts and handle payments.
Contributing

Contributions are welcome! Please fork the repository and create a pull request with your changes.

License
This project is licensed under the MIT License. See the LICENSE file for more details.

Contact
If you have any questions or feedback, please contact us at [Harshsahu1143@gmail.com].
