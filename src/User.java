public class User {
    public int userId;
    public String fullName, email, passwordHash, role;

    public User(int id, String name, String email, String pass, String role) {
        this.userId = id;
        this.fullName = name;
        this.email = email;
        this.passwordHash = pass;
        this.role = role;
    }

    @Override
    public String toString() {
        return fullName + " (" + role + ")";
    }
}
