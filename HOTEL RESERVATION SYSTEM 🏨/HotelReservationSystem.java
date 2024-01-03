package Hotel_Reservation_System;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.Scanner;
import java.sql.Statement;
import java.sql.ResultSet;

public class HotelReservationSystem {
    private static final String url = "jdbc:mysql://localhost:3306/hotel_db";
    private static final String username = "root";
    private static final String password = "root";

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
        }catch(ClassNotFoundException e){
            System.out.println(e.getMessage());
        }

        try{
            Connection connection = DriverManager.getConnection(url, username, password);
            while(true){
                Scanner input = new Scanner(System.in);
                System.out.println("\nHOTEL MANAGEMENT SYSTEM");
                System.out.println("1. Reserve A Room");
                System.out.println("2. View Reservations");
                System.out.println("3. Get Room Number");
                System.out.println("4. Update Reservation");
                System.out.println("5. Delete Reservation");
                System.out.println("0. Exit");
                System.out.print("\nChoose An Option: ");
                int choice = input.nextInt();

                switch(choice){
                    case 1:
                        reserveRoom(connection, input);
                        break;
                    case 2:
                        viewReservation(connection);
                        break;
                    case 3:
                        getRoomNumber(connection, input);
                        break;
                    case 4:
                        updateReservation(connection, input);
                        break;
                    case 5:
                        deleteReservation(connection, input);
                        break;
                    case 0:
                        exit();
                        input.close();
                        return;
                    default:
                        System.out.println("Invalid Choice, Try Again.");
                }
            }
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }catch (InterruptedException e){
            throw new RuntimeException(e);
        }
    }

    private static void reserveRoom(Connection connection, Scanner input){
        try{
            System.out.print("Enter Guest Name: ");
            String guestName = input.next();
            input.nextLine();
            System.out.print("Enter Room Number: ");
            int roomNumber = input.nextInt();
            System.out.print("Enter Contact Number: ");
            String contactNumber = input.next();

            String sql = "Insert Into reservation (guest_name, room_number, contact_number) "+
                    "Values ('"+guestName+"', "+roomNumber+", '"+contactNumber+"');";

            try(Statement statement = connection.createStatement()){

                int affectedRow = statement.executeUpdate(sql);
                if(affectedRow>0){
                    System.out.println("\nReservation Successful..!!!");
                }else{
                    System.out.println("\nReservation Failed..!!!");
                }
            }
        }catch(SQLException e){
            e.getStackTrace();
        }
    }

    private static void viewReservation(Connection connection) throws SQLException{
        String sql = "SELECT reservation_id, guest_name, room_number, contact_number, reservation_date From reservation;";
        try(Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql)){

            System.out.println("Current Reservation: ");
            System.out.println("+----------------+-----------------+-----------------+-------------------+---------------------+");
            System.out.println("| Reservation ID | Guest           |   Room Number   |   Contact Number  | Reservation Date    |");
            System.out.println("+----------------+-----------------+-----------------+-------------------+---------------------+");

            while(resultSet.next()){
                int reservationId = resultSet.getInt("reservation_id");
                String guestName = resultSet.getString("guest_name");
                int roomNumber = resultSet.getInt("room_number");
                String contactNumber = resultSet.getString("contact_number");
                String reservationDate = resultSet.getTimestamp("reservation_date").toString();

                System.out.printf("| %-14d | %-15s | %-13d | %-20s | %-19s |\n",
                        reservationId, guestName, roomNumber, contactNumber, reservationDate);
                System.out.println("+----------------+--------------+-----------------+-------------------+---------------------+");

            }
        }
    }

    private static void getRoomNumber(Connection connection, Scanner input){
        try{
            System.out.print("Enter Reservation ID: ");
            int reservationId = input.nextInt();
            System.out.print("Enter Guest Name: ");
            String guestName = input.next();

            String sql = "SELECT room_number FROM reservation WHERE reservation_id = "+reservationId+
                    " AND guest_name = '"+guestName+"';";

            try(Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)){
                if(resultSet.next()){
                    int roomNumber = resultSet.getInt("room_number");
                    System.out.println("Room number for Reservation ID "+reservationId+
                            " and Guest "+guestName+" is: "+roomNumber);
                }else{
                    System.out.println("Reservation not found for the given ID and guest name.");
                }
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    private static void updateReservation(Connection connection, Scanner input){
        try{
            System.out.print("Enter reservation ID to update: ");
            int reservationId = input.nextInt();
            input.nextLine();

            if(!reservationExist(connection, reservationId)){
                System.out.println("Reservation not found for the given ID.");
                return;
            }

            System.out.print("Enter New Guest Name: ");
            String newGuestName = input.nextLine();
            System.out.print("Enter New Room Number: ");
            int newRoomNumber = input.nextInt();
            System.out.print("Enter New Contact Number: ");
            String newContactNumber = input.next();

            String sql = "UPDATE reservation SET guest_name = '"+newGuestName+"', "+
                    "room_number = "+newRoomNumber+", "+
                    "contact_number = '"+newContactNumber+"' "+
                    "WHERE reservation_id = "+reservationId+";";

            try(Statement statement = connection.createStatement()){
                int affectedRows = statement.executeUpdate(sql);

                if(affectedRows>0){
                    System.out.println("Reservation updated successfully!");
                }else{
                    System.out.println("Reservation update failed.");
                }
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    private static void deleteReservation(Connection connection, Scanner input){
        try{
            System.out.print("Enter reservation ID to Delet: ");
            int resevationId = input.nextInt();

            if(!reservationExist(connection, resevationId)){
                System.out.println("Reservation not found for the given ID.");
                return;
            }

            String sql = "DELETE FROM reservation where reservation_id = "+resevationId+";";

            try(Statement statement = connection.createStatement()){
                int affetedRow = statement.executeUpdate(sql);
                if(affetedRow>0){
                    System.out.println("Reservation deleted successfully.");
                }else{
                    System.out.println("Reservation deletion failed.");
                }
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    private static boolean reservationExist(Connection connection, int reservationId){
        try{
            String sql = "SELECT reservation_id FROM reservation WHERE reservation_id = "+reservationId+";";

            try(Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)){
                return resultSet.next();
            }
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public static void exit() throws InterruptedException{
        System.out.print("Exiting System");
        int i=5;
        while(i>0){
            System.out.print(".");
            Thread.sleep(500);
            i--;
        }
        System.out.println("\nThank You For Using Hotel Reservation System.!!!");
    }
}
