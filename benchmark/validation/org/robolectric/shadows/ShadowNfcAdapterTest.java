package org.robolectric.shadows;


import NfcAdapter.CreateNdefMessageCallback;
import NfcAdapter.OnNdefPushCompleteCallback;
import android.app.Activity;
import android.app.Application;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;


@RunWith(AndroidJUnit4.class)
public class ShadowNfcAdapterTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private Application context;

    @Test
    public void setNdefPushMesageCallback_shouldUseCallback() {
        final NfcAdapter.CreateNdefMessageCallback callback = Mockito.mock(CreateNdefMessageCallback.class);
        final Activity activity = Robolectric.setupActivity(Activity.class);
        final NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);
        adapter.setNdefPushMessageCallback(callback, activity);
        assertThat(Shadows.shadowOf(adapter).getNdefPushMessageCallback()).isSameAs(callback);
    }

    @Test
    public void setOnNdefPushCompleteCallback_shouldUseCallback() {
        final NfcAdapter.OnNdefPushCompleteCallback callback = Mockito.mock(OnNdefPushCompleteCallback.class);
        final Activity activity = Robolectric.setupActivity(Activity.class);
        final NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);
        adapter.setOnNdefPushCompleteCallback(callback, activity);
        assertThat(Shadows.shadowOf(adapter).getOnNdefPushCompleteCallback()).isSameAs(callback);
    }

    @Test
    public void setOnNdefPushCompleteCallback_throwsOnNullActivity() {
        final NfcAdapter.OnNdefPushCompleteCallback callback = Mockito.mock(OnNdefPushCompleteCallback.class);
        final Activity activity = Robolectric.setupActivity(Activity.class);
        final Activity nullActivity = null;
        final NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("activity cannot be null");
        adapter.setOnNdefPushCompleteCallback(callback, nullActivity);
    }

    @Test
    public void setOnNdefPushCompleteCallback_throwsOnNullInActivities() {
        final NfcAdapter.OnNdefPushCompleteCallback callback = Mockito.mock(OnNdefPushCompleteCallback.class);
        final Activity activity = Robolectric.setupActivity(Activity.class);
        final Activity nullActivity = null;
        final NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("activities cannot contain null");
        adapter.setOnNdefPushCompleteCallback(callback, activity, nullActivity);
    }

    @Test
    public void isEnabled_shouldReturnEnabledState() {
        final NfcAdapter adapter = NfcAdapter.getDefaultAdapter(context);
        assertThat(adapter.isEnabled()).isFalse();
        Shadows.shadowOf(adapter).setEnabled(true);
        assertThat(adapter.isEnabled()).isTrue();
        Shadows.shadowOf(adapter).setEnabled(false);
        assertThat(adapter.isEnabled()).isFalse();
    }

    @Test
    public void getNfcAdapter_returnsNonNull() {
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(context);
        assertThat(adapter).isNotNull();
    }

    @Test
    public void getNfcAdapter_hardwareExists_returnsNonNull() {
        ShadowNfcAdapter.setNfcHardwareExists(true);
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(context);
        assertThat(adapter).isNotNull();
    }

    @Test
    public void getNfcAdapter_hardwareDoesNotExist_returnsNull() {
        ShadowNfcAdapter.setNfcHardwareExists(false);
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(context);
        assertThat(adapter).isNull();
    }

    @Test
    public void setNdefPushMessage_setsNullMessage() {
        final Activity activity = Robolectric.setupActivity(Activity.class);
        final NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);
        adapter.setNdefPushMessage(null, activity);
        assertThat(Shadows.shadowOf(adapter).getNdefPushMessage()).isNull();
    }

    @Test
    public void setNdefPushMessage_setsNonNullMessage() throws Exception {
        final Activity activity = Robolectric.setupActivity(Activity.class);
        final NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);
        final NdefMessage message = new NdefMessage(new NdefRecord[]{ new NdefRecord(NdefRecord.TNF_EMPTY, null, null, null) });
        adapter.setNdefPushMessage(message, activity);
        assertThat(Shadows.shadowOf(adapter).getNdefPushMessage()).isSameAs(message);
    }

    @Test
    public void getNdefPushMessage_messageNotSet_throwsIllegalStateException() throws Exception {
        final Activity activity = Robolectric.setupActivity(Activity.class);
        final NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);
        expectedException.expect(IllegalStateException.class);
        Shadows.shadowOf(adapter).getNdefPushMessage();
    }
}
