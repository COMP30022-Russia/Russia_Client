package com.comp30022.team_russia.assist.features.assoc.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.comp30022.team_russia.assist.features.home_contacts.models.ContactListItemData;
import com.comp30022.team_russia.assist.features.message.models.Association;
import com.comp30022.team_russia.assist.features.message.models.UserContact;

import java.util.List;

/**
 * Data Access Object for {@link UserContact} and {@link Association}.
 */
@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdateAssociation(Association association);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdateUserProfile(UserContact user);

    @Query("DELETE FROM association_table")
    void clearAssociations();

    @Query("DELETE FROM user_profile_table")
    void clearUserProfiles();

    @Query("SELECT "
           + "associationId,"
           + "name,"
           + "userId,"
           + "lastMessage,"
           + "(lastMessageId > lastReadId) as hasUnread "
           + "FROM (SELECT "
           + "ac.id as associationId,"
           + "u.name as name,"
           + "u.id as userId,"
           + "ifnull( "
           + "  (SELECT "
           + "      content"
           + "      FROM message_table AS m1"
           + "      WHERE m1.associationId = ac.id"
           + "      ORDER BY createdAt DESC"
           + "      LIMIT 1),"
           + "  'No messages') as lastMessage,"
           + "ifnull( "
           + "  (SELECT MAX(id)"
           + "      FROM message_table AS m3"
           + "      WHERE m3.associationId = ac.id"
           + "  ),"
           + "  0) as lastMessageId,"
           + "ifnull("
           + "  (SELECT MAX(createdAt)"
           + "      FROM message_table AS m2"
           + "      WHERE m2.associationId = ac.id"
           + "  ),"
           + "  datetime('now','localtime')) as lastMessageTime,"
           + "ifnull("
           + "  (SELECT lastReadId"
           + "      FROM chat_read_pointers AS r"
           + "      WHERE r.id = ac.id)"
           + "  ,0) as lastReadId "
           + "FROM association_table as ac "
           + "INNER JOIN user_profile_table as u ON ac.userId = u.id "
           + "WHERE ac.isActive = 1)"
           + "ORDER BY lastMessageTime DESC")
    LiveData<List<ContactListItemData>> getContactList();
}
