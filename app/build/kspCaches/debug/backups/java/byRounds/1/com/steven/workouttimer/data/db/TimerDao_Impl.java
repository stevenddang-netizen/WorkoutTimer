package com.steven.workouttimer.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TimerDao_Impl implements TimerDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TimerEntity> __insertionAdapterOfTimerEntity;

  private final EntityDeletionOrUpdateAdapter<TimerEntity> __deletionAdapterOfTimerEntity;

  private final EntityDeletionOrUpdateAdapter<TimerEntity> __updateAdapterOfTimerEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteTimerById;

  public TimerDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTimerEntity = new EntityInsertionAdapter<TimerEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `timers` (`id`,`name`,`totalMinutes`,`audioEnabled`,`audioType`,`countdownSeconds`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TimerEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindLong(3, entity.getTotalMinutes());
        final int _tmp = entity.getAudioEnabled() ? 1 : 0;
        statement.bindLong(4, _tmp);
        statement.bindString(5, entity.getAudioType());
        statement.bindLong(6, entity.getCountdownSeconds());
        statement.bindLong(7, entity.getCreatedAt());
      }
    };
    this.__deletionAdapterOfTimerEntity = new EntityDeletionOrUpdateAdapter<TimerEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `timers` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TimerEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfTimerEntity = new EntityDeletionOrUpdateAdapter<TimerEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `timers` SET `id` = ?,`name` = ?,`totalMinutes` = ?,`audioEnabled` = ?,`audioType` = ?,`countdownSeconds` = ?,`createdAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TimerEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindLong(3, entity.getTotalMinutes());
        final int _tmp = entity.getAudioEnabled() ? 1 : 0;
        statement.bindLong(4, _tmp);
        statement.bindString(5, entity.getAudioType());
        statement.bindLong(6, entity.getCountdownSeconds());
        statement.bindLong(7, entity.getCreatedAt());
        statement.bindLong(8, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteTimerById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM timers WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertTimer(final TimerEntity timer, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfTimerEntity.insertAndReturnId(timer);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteTimer(final TimerEntity timer, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfTimerEntity.handle(timer);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateTimer(final TimerEntity timer, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfTimerEntity.handle(timer);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteTimerById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteTimerById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteTimerById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<TimerEntity>> getAllTimers() {
    final String _sql = "SELECT * FROM timers ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"timers"}, new Callable<List<TimerEntity>>() {
      @Override
      @NonNull
      public List<TimerEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfTotalMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "totalMinutes");
          final int _cursorIndexOfAudioEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "audioEnabled");
          final int _cursorIndexOfAudioType = CursorUtil.getColumnIndexOrThrow(_cursor, "audioType");
          final int _cursorIndexOfCountdownSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "countdownSeconds");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<TimerEntity> _result = new ArrayList<TimerEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TimerEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpTotalMinutes;
            _tmpTotalMinutes = _cursor.getInt(_cursorIndexOfTotalMinutes);
            final boolean _tmpAudioEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAudioEnabled);
            _tmpAudioEnabled = _tmp != 0;
            final String _tmpAudioType;
            _tmpAudioType = _cursor.getString(_cursorIndexOfAudioType);
            final int _tmpCountdownSeconds;
            _tmpCountdownSeconds = _cursor.getInt(_cursorIndexOfCountdownSeconds);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new TimerEntity(_tmpId,_tmpName,_tmpTotalMinutes,_tmpAudioEnabled,_tmpAudioType,_tmpCountdownSeconds,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getTimerById(final long id, final Continuation<? super TimerEntity> $completion) {
    final String _sql = "SELECT * FROM timers WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<TimerEntity>() {
      @Override
      @Nullable
      public TimerEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfTotalMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "totalMinutes");
          final int _cursorIndexOfAudioEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "audioEnabled");
          final int _cursorIndexOfAudioType = CursorUtil.getColumnIndexOrThrow(_cursor, "audioType");
          final int _cursorIndexOfCountdownSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "countdownSeconds");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final TimerEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpTotalMinutes;
            _tmpTotalMinutes = _cursor.getInt(_cursorIndexOfTotalMinutes);
            final boolean _tmpAudioEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAudioEnabled);
            _tmpAudioEnabled = _tmp != 0;
            final String _tmpAudioType;
            _tmpAudioType = _cursor.getString(_cursorIndexOfAudioType);
            final int _tmpCountdownSeconds;
            _tmpCountdownSeconds = _cursor.getInt(_cursorIndexOfCountdownSeconds);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new TimerEntity(_tmpId,_tmpName,_tmpTotalMinutes,_tmpAudioEnabled,_tmpAudioType,_tmpCountdownSeconds,_tmpCreatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
