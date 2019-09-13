package models;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.joda.time.LocalDateTime;

import java.util.UUID;

@Entity
@Keep
public class mNotification {
    @PrimaryKey
    @NonNull
    public String _id = UUID.randomUUID().toString();
    public String date = LocalDateTime.now().toString();
    public String notification_text;
    public boolean is_read;
}
