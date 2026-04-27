import java.util.*;

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
        users.add(new User(1, "Michelle Machate", "michelle@spu.ac.za", "pass123", "student"));
        users.add(new User(2, "Thabo Mokoena", "thabo@spu.ac.za", "pass123", "student"));
        users.add(new User(3, "Lerato Dlamini", "lerato@spu.ac.za", "pass123", "student"));
        users.add(new User(4, "James Sithole", "james@spu.ac.za", "staff123", "staff"));
        users.add(new User(5, "Nomsa Khumalo", "nomsa@spu.ac.za", "staff123", "staff"));
        users.add(new User(6, "Prof. Mamabolo", "manager@uni.ac.za", "mgr123", "manager"));

        // Locations
        locations.add(new Location(1, "Lecture Hall A101", "Block A", "Lecture Hall", -25.7461, 28.1881, "QR-LHA101"));
        locations.add(new Location(2, "Computer Lab B204", "Block B", "IT Lab", -25.7465, 28.1885, "QR-LAB204"));
        locations.add(new Location(3, "Main Library", "Central", "Library", -25.7470, 28.1890, "QR-MLIB01"));
        locations.add(new Location(4, "Admin Office", "Block C", "Admin Office", -25.7455, 28.1875, "QR-ADOFF1"));
        locations.add(new Location(5, "Residence Block C", "Res Area", "Residence", -25.7480, 28.1870, "QR-RESC01"));
        locations.add(new Location(6, "Student Centre", "Central", "Support", -25.7468, 28.1888, "QR-STCTR1"));

        // Services
        services.add(new Service(1, "IT Support", "Hardware/software issues", "IT Dept", 2));
        services.add(new Service(2, "Academic Advising", "Course & module guidance", "Academic Dept", 4));
        services.add(new Service(3, "Registration Help", "Enrolment queries", "Registry", 4));
        services.add(new Service(4, "Counselling", "Student wellness support", "Student Svcs", 6));
        services.add(new Service(5, "Residence Maint.", "Maintenance requests", "Facilities", 5));
        services.add(new Service(6, "Library Services", "Books, printing, research", "Library", 3));

        // Requests
        requests.add(new Request(1, 1, 1, 2, "Projector not working in lab", "In Progress", "2026-04-20"));
        requests.add(new Request(2, 1, 3, 4, "Need help with late registration", "Resolved", "2026-04-15"));
        requests.add(new Request(3, 2, 5, 5, "Broken window in room 14", "Submitted", "2026-04-25"));
        requests.add(new Request(4, 2, 4, 6, "Struggling academically, need counselling", "Submitted", "2026-04-26"));

        // Updates
        requestUpdates.add(new RequestUpdate(1, 1, 3, "Assigned to tech team. Will inspect tomorrow.", "2026-04-21"));
        requestUpdates.add(new RequestUpdate(2, 2, 4, "Registration processed. Student enrolled.", "2026-04-16"));
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
