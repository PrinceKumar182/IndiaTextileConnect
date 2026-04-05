import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestDb {
    public static void main(String[] args) {
        String uri = "mongodb+srv://prince21182_db_user:u1YTb1sqeBJQKSb6@cluster0.uz6uptp.mongodb.net/textile";
        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase database = mongoClient.getDatabase("textile");
            MongoCollection<Document> collection = database.getCollection("users");
            
            Document user = collection.find(new Document("phone", "9166015342")).first();
            if (user != null) {
                System.out.println("Found user: " + user.toJson());
                
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                String testPassword = "newPassword123";
                String encoded = encoder.encode(testPassword);
                
                System.out.println("Encoded new password: " + encoded);
                System.out.println("Does match after encode? " + encoder.matches(testPassword, encoded));
                
                System.out.println("Current saved password: " + user.getString("password"));
                System.out.println("Matches current saved? " + encoder.matches(testPassword, user.getString("password")));
                
            } else {
                System.out.println("User not found!");
            }
        }
    }
}
