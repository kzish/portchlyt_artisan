package models;

import org.joda.time.LocalDateTime;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class mNotification extends RealmObject {
    @PrimaryKey
    public String _id = UUID.randomUUID().toString();
    public String date = LocalDateTime.now().toString();
    public String notification_text;
    public boolean is_read;
}
