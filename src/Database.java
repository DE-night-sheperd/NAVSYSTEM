import java.util.*;
import java.io.*;

public class Database {
    public static List<User> users = new ArrayList<>();
    public static List<Location> locations = new ArrayList<>();
    public static List<Service> services = new ArrayList<>();
    public static List<Request> requests = new ArrayList<>();
    public static List<RequestUpdate> requestUpdates = new ArrayList<>();
    
    public static User currentUser = null;

    public static int nextUserId = 10;
    public static int nextLocId = 10;
    public static int nextSvcId = 10;
    public static int nextReqId = 10;
    public static int nextUpdId = 10;

    public static void seedData() {
        // Users
        loadUsers();
        
        // SPU Locations - Central Campus
        locations.add(new Location(1, "Moroka Hall (C001)", "Central Campus", "Residence", -28.7490, 24.7645, "QR-C001"));
        locations.add(new Location(8, "Student Affairs (C002)", "Central Campus", "Admin", -28.7485, 24.7650, "QR-C002"));
        locations.add(new Location(3, "Academic Building (C003)", "Central Campus", "Lecture Hall", -28.749503029197044, 24.764863105051763, "QR-C003"));
        locations.add(new Location(2, "Library & Resources (C004)", "Central Campus", "Library", -28.748999,  24.765028, "QR-C004"));
        locations.add(new Location(9, "Natural Sciences (C005)", "Central Campus", "Science", -28.749681061286545, 24.765182736905615, "QR-C005"));
        locations.add(new Location(4, "Data Science & IT (C006)", "Central Campus", "IT Lab", -28.749690964132864, 24.76451914303902, "QR-C006"));
        locations.add(new Location(10, "Humanities Labs (C010)", "Central Campus", "Arts", -28.750299987377037, 24.764327124388263, "QR-C010"));
        locations.add(new Location(11, "Agricultural Sciences (C011)", "Central Campus", "Agriculture", -28.75032722004077, 24.764965304021665, "QR-C011"));

        // South Campus
        locations.add(new Location(5, "Windhoek Draught Park", "South Campus", "Sports", -28.755022363526145, 24.75802464368211, "QR-WDP"));
        locations.add(new Location(12, "Massouw Hall (S007)", "South Campus", "Residence", -28.753528, 24.760333, "QR-S007"));
        locations.add(new Location(6, "Umnandi Hall (S005)", "South Campus", "Residence", -28.754926624260552, 24.75983867680364, "QR-S005"));
        locations.add(new Location(13, "Tauana Hall (R002)", "South Campus", "Residence", -28.75559720350166, 24.760054269382643, "QR-R002"));

        // North Campus
        locations.add(new Location(7, "North Campus Admin", "North Campus", "Admin Office", -28.745111, 24.766583, "QR-NCADMIN"));

        // Services
        services.add(new Service(1, "IT Support", "Hardware/software issues", "IT Dept", 2, "ext 101"));
        services.add(new Service(2, "Academic Advising", "Course & module guidance", "Academic Dept", 4, "advise@spu.ac.za"));
        services.add(new Service(3, "Registration Help", "Enrolment queries", "Registry", 4, "reg@spu.ac.za"));
        services.add(new Service(4, "Counselling", "Student wellness support", "Student Svcs", 6, "wellness@spu.ac.za"));
        services.add(new Service(5, "Residence Maint.", "Maintenance requests", "Facilities", 5, "maint@spu.ac.za"));
        services.add(new Service(6, "Library Services", "Books, printing, research", "Library", 3, "library@spu.ac.za"));

        // Requests
        requests.add(new Request(1, 1, 1, 2, "Projector not working in lab", "In Progress", "2026-04-20"));
        requests.add(new Request(2, 1, 3, 4, "Need help with late registration", "Resolved", "2026-04-15"));
        requests.add(new Request(3, 2, 5, 5, "Broken window in room 14", "Submitted", "2026-04-25"));
        requests.add(new Request(4, 2, 4, 6, "Struggling academically, need counselling", "Submitted", "2026-04-26"));

        // Updates
        requestUpdates.add(new RequestUpdate(1, 1, 3, "Assigned to tech team. Will inspect tomorrow.", "2026-04-21"));
        requestUpdates.add(new RequestUpdate(2, 2, 4, "Registration processed. Student enrolled.", "2026-04-16"));
    }

    public static void loadUsers() {
        File file = new File("users.txt");
        if (!file.exists()) {
            users.add(new User(1, "Michelle Machate", "michelle@spu.ac.za", "pass123", "student"));
            users.add(new User(2, "Thabo Mokoena", "thabo@spu.ac.za", "pass123", "student"));
            users.add(new User(3, "Lerato Dlamini", "lerato@spu.ac.za", "pass123", "student"));
            users.add(new User(4, "James Sithole", "james@spu.ac.za", "staff123", "staff"));
            users.add(new User(5, "Nomsa Khumalo", "nomsa@spu.ac.za", "staff123", "staff"));
            users.add(new User(6, "Choza Mamabolo", "manager@uni.ac.za", "mgr123", "manager"));
            saveUsers();
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int maxId = 9;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    int id = Integer.parseInt(parts[0].trim());
                    users.add(new User(id, parts[1].trim(), parts[2].trim(), parts[3].trim(), parts[4].trim()));
                    if (id > maxId) maxId = id;
                }
            }
            nextUserId = maxId + 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveUsers() {
        try (PrintWriter pw = new PrintWriter(new FileWriter("users.txt"))) {
            for (User u : users) {
                pw.println(u.userId + "," + u.name + "," + u.email + "," + u.passwordHash + "," + u.role);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addUser(User user) {
        users.add(user);
        saveUsers();
    }

    public static User findUser(int id) {
        for (User u : users) if (u.userId == id) return u;
        return null;
    }

    public static Location findLocation(int id) {
        for (Location l : locations) if (l.locationId == id) return l;
        return null;
    }

    public static Service findService(int id) {
        for (Service s : services) if (s.serviceId == id) return s;
        return null;
    }

    public static Request findRequest(int id) {
        for (Request r : requests) if (r.requestId == id) return r;
        return null;
    }

    public static User authenticate(String email, String pass) {
        for (User u : users) {
            if (u.email.equalsIgnoreCase(email) && u.passwordHash.equals(pass)) return u;
        }
        return null;
    }
}
