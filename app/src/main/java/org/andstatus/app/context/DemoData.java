/*
 * Copyright (C) 2017 yvolk (Yuri Volkov), http://yurivolkov.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.andstatus.app.context;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.andstatus.app.account.DemoAccountInserter;
import org.andstatus.app.account.MyAccount;
import org.andstatus.app.backup.ProgressLogger;
import org.andstatus.app.data.DbUtils;
import org.andstatus.app.data.DemoConversationInserter;
import org.andstatus.app.data.DemoGnuSocialConversationInserter;
import org.andstatus.app.data.checker.CheckConversations;
import org.andstatus.app.net.social.Actor;
import org.andstatus.app.origin.DemoOriginInserter;
import org.andstatus.app.origin.OriginPumpio;
import org.andstatus.app.origin.OriginType;
import org.andstatus.app.os.AsyncTaskLauncher;
import org.andstatus.app.os.MyAsyncTask;
import org.andstatus.app.service.MyServiceManager;
import org.andstatus.app.timeline.meta.Timeline;
import org.andstatus.app.timeline.meta.TimelineType;
import org.andstatus.app.util.MyLog;
import org.andstatus.app.util.TriState;

import java.util.Locale;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public final class DemoData {
    public static volatile DemoData demoData = new DemoData();
    private static final String TAG = DemoData.class.getSimpleName();
    private static final String TAG_ASYNC = TAG + "Async";
    private static final String HTTP = "http://";

    public final String testRunUid = String.valueOf(System.currentTimeMillis());

    public final String testOriginParentHost = "example.com";
    public final String pumpioOriginName = "PumpioTest";
    public final String pumpioMainHost = "pump1." + testOriginParentHost;
    public final String pumpioTestAccountUsername = "t131t@" + pumpioMainHost;
    public final String pumpioTestAccountName = pumpioTestAccountUsername + "/" + pumpioOriginName;
    public final String pumpioTestAccountActorOid = OriginPumpio.ACCOUNT_PREFIX + pumpioTestAccountUsername;

    public final String gnusocialTestOriginName = "GNUsocialTest";
    public final String gnusocialTestAccountUsername = "t131t";
    public final String gnusocialTestAccountName = gnusocialTestAccountUsername + "/" + gnusocialTestOriginName;
    public final String gnusocialTestAccountActorOid = "115391";
    public final String gnusocialTestAccountAvatarUrl = "https://findicons.com/files/icons/2036/farm/48/rabbit.png";
    public final String gnusocialTestAccount2Username = "gtester2";
    public final String gnusocialTestAccount2Name = gnusocialTestAccount2Username + "/" + gnusocialTestOriginName;
    public final String gnusocialTestAccount2ActorOid = "8902454";

    public final String twitterTestOriginName = "TwitterTest";
    public final String twitterTestAccountUsername = "t131t";
    public final String twitterTestAccountActorOid = "144771645";
    public final String twitterTestAccountName = twitterTestAccountUsername + "/" + twitterTestOriginName;

    public final String mastodonTestOriginName = "MastodonTest";
    public final String mastodonTestAccountUsername = "t131t1";
    public final String mastodonTestAccountName = mastodonTestAccountUsername + "/" + mastodonTestOriginName;
    public final String mastodonTestAccountActorOid = "37";

    public final OriginType conversationOriginType = OriginType.PUMPIO;
    public final String conversationOriginName = pumpioOriginName;
    private final String conversationAccountUsername = "testerofandstatus@" + pumpioMainHost;
    public final String conversationAccountName = conversationAccountUsername + "/" + conversationOriginName;
    public final String conversationAccountActorOid = OriginPumpio.ACCOUNT_PREFIX + conversationAccountUsername;
    public final String conversationAccountAvatarUrl = "http://andstatus.org/images/andstatus-logo.png";
    public final String conversationEntryNoteOid = HTTP + pumpioMainHost
            + "/testerofandstatus/comment/thisisfakeuri" + testRunUid;
    public final String conversationEntryAuthorOid = "acct:first@pumpentry.example.com";
    public final String conversationAuthorSecondUsername = "second@" + pumpioMainHost;
    public final String conversationAuthorSecondActorOid = OriginPumpio.ACCOUNT_PREFIX + conversationAuthorSecondUsername;
    public final String conversationAuthorThirdUsername = "third@pump3.example.com";
    public final String conversationAuthorThirdActorOid = OriginPumpio.ACCOUNT_PREFIX + conversationAuthorThirdUsername;
    public final String conversationMentionsNoteOid = HTTP + pumpioMainHost + "/second/comment/replywithmentions" + testRunUid;
    public final String conversationMentionOfAuthor3Oid = HTTP + pumpioMainHost + "/second/comment/mention3" + testRunUid;
    public final String htmlNoteOid = HTTP + pumpioMainHost + "/testerofandstatus/comment/htmlfakeuri" + testRunUid;
    public final String conversationAccount2Username = "tester2ofandstatus@" + pumpioMainHost;
    public final String conversationAccount2Name = conversationAccount2Username + "/" + conversationOriginName;
    public final String conversationAccount2ActorOid = OriginPumpio.ACCOUNT_PREFIX + conversationAccount2Username;

    public final String plainTextNoteOid = "2167283" + testRunUid;
    public final String publicNoteText = "UniqueText" + testRunUid;
    public final String globalPublicNoteText = "Public_in_AndStatus_" + testRunUid;
    /** See http://stackoverflow.com/questions/6602417/get-the-uri-of-an-image-stored-in-drawable */
    public final Uri localImageTestUri = Uri.parse("android.resource://org.andstatus.app.tests/drawable/icon");
    public final Uri localImageTestUri2 = Uri.parse("android.resource://org.andstatus.app/drawable/splash_logo");
    public final Uri image1Url = Uri.parse("https://raw.githubusercontent.com/andstatus/andstatus/master/app/src/main/res/drawable/splash_logo.png");

    private volatile String dataPath = "";

    private DemoData() {
        DemoConversationInserter.onNewDemoData();
    }

    void createNewInstance() {
        demoData = new DemoData();
    }

    public String getTestOriginHost(String testOriginName) {
        String host = testOriginName.toLowerCase(Locale.US) + "." + testOriginParentHost;
        if (testOriginName.equalsIgnoreCase(twitterTestOriginName)) {
            host = "api." + host;
        }
        return host;
    }

    public void add(final MyContext myContext, String dataPathIn) {
        final String method = "add";
        dataPath = dataPathIn;
        MyLog.v(TAG, method + ": started");
        final MyAsyncTask<Void, Void, Void> asyncTask = addAsync(method, myContext, null);
        long count = 50;
        while (count > 0) {
            boolean completedWork = asyncTask.completedBackgroundWork();
            MyLog.v(method, (completedWork ? "Task completed " : "Waiting for task completion ") + count + " "
                    + asyncTask.getStatus());
            if (completedWork || DbUtils.waitMs(method, 5000)) {
                break;
            }
            count--;
        }
        assertEquals("Demo data creation failed, count=" + count + ", status=" + asyncTask.getStatus()
                + ", " + asyncTask.toString(), true, asyncTask.completedBackgroundWork());
        assertTrue("Error during Demo data creation: " + asyncTask.getFirstError(), asyncTask.getFirstError().isEmpty());
        MyLog.v(TAG, method + ": ended");
    }

    @NonNull
    public MyAsyncTask<Void, Void, Void> addAsync(final String method, final MyContext myContext,
                                                          final ProgressLogger.ProgressCallback progressCallback) {
        final MyAsyncTask<Void, Void, Void> asyncTask
                = new MyAsyncTask<Void, Void, Void>(method, MyAsyncTask.PoolEnum.QUICK_UI) {
            @Override
            protected Void doInBackground2(Void... params) {
                MyLog.i(TAG_ASYNC, method + ": started");
                if (progressCallback != null) {
                    DbUtils.waitMs(TAG_ASYNC, 3000);
                    progressCallback.onProgressMessage("Generating demo data...");
                    DbUtils.waitMs(TAG_ASYNC, 1000);
                }
                MyLog.v(TAG_ASYNC, "Before initialize 1");
                MyContextHolder.initialize(myContext.context(), method);
                MyLog.v(TAG_ASYNC, "After initialize 1");
                MyServiceManager.setServiceUnavailable();
                DemoOriginInserter originInserter = new DemoOriginInserter(myContext);
                originInserter.insert();
                final DemoAccountInserter accountInserter = new DemoAccountInserter(myContext);
                accountInserter.insert();
                myContext.persistentTimelines().saveChanged();

                MyPreferences.onPreferencesChanged();
                MyContextHolder.setExpiredIfConfigChanged();
                MyLog.v(TAG_ASYNC, "Before initialize 2");
                MyContextHolder.initialize(myContext.context(), method);
                MyLog.v(TAG_ASYNC, "After initialize 2");
                MyServiceManager.setServiceUnavailable();
                if (progressCallback != null) {
                    progressCallback.onProgressMessage("Demo accounts added...");
                    DbUtils.waitMs(TAG_ASYNC, 1000);
                }
                assertTrue("Context is not ready", MyContextHolder.get().isReady());
                checkDataPath();
                int size = MyContextHolder.get().persistentAccounts().size();
                assertTrue("Only " + size + " accounts added: " + MyContextHolder.get().persistentAccounts(), size > 5);
                originInserter.checkDefaultTimelinesForOrigins();
                accountInserter.checkDefaultTimelinesForAccounts();

                new DemoConversationInserter().insertConversation("");
                new DemoGnuSocialConversationInserter().insertConversation();
                if (progressCallback != null) {
                    progressCallback.onProgressMessage("Demo notes added...");
                    DbUtils.waitMs(TAG_ASYNC, 1000);
                }
                if (MyContextHolder.get().persistentAccounts().size() == 0) {
                    fail("No persistent accounts");
                }
                setSuccessfulAccountAsCurrent();
                Timeline defaultTimeline = MyContextHolder.get().persistentTimelines().getFiltered(
                        false, TriState.TRUE, TimelineType.EVERYTHING, null,
                        MyContextHolder.get().persistentAccounts().getCurrentAccount().getOrigin()).get(0);
                assertThat(defaultTimeline.getTimelineType(), is(TimelineType.EVERYTHING));
                MyContextHolder.get().persistentTimelines().setDefault(defaultTimeline);
                MyLog.v(TAG_ASYNC, "Before initialize 3");
                MyContextHolder.initialize(myContext.context(), method);
                assertConversations();
                MyLog.v(TAG_ASYNC, "After initialize 3");
                if (progressCallback != null) {
                    progressCallback.onProgressMessage("Demo data is ready");
                    DbUtils.waitMs(TAG_ASYNC, 1000);
                }
                MyLog.i(TAG_ASYNC, method + ": ended");
                return null;
            }

            @Override
            protected void onFinish(Void aVoid, boolean success) {
                if (progressCallback != null) {
                    progressCallback.onComplete(success);
                }
            }
        };
        AsyncTaskLauncher.execute(method, true, asyncTask);
        return asyncTask;
    }

    public void assertConversations() {
        assertEquals("Conversations need fixes", 0, new CheckConversations()
                        .setMyContext(MyContextHolder.get()).setLogger(ProgressLogger.getEmpty()).countChanges());
    }

    private void setSuccessfulAccountAsCurrent() {
        MyLog.i(TAG, "Persistent accounts: " + MyContextHolder.get().persistentAccounts().size());
        boolean found = (MyContextHolder.get().persistentAccounts().getCurrentAccount().getCredentialsVerified()
                == MyAccount.CredentialsVerificationStatus.SUCCEEDED);
        if (!found) {
            for (MyAccount ma : MyContextHolder.get().persistentAccounts().list()) {
                MyLog.i(TAG, ma.toString());
                if (ma.getCredentialsVerified()
                == MyAccount.CredentialsVerificationStatus.SUCCEEDED) {
                    found = true;
                    MyContextHolder.get().persistentAccounts().setCurrentAccount(ma);
                    break;
                }
            }
        }
        assertTrue("Found account, which is successfully verified", found);
        assertTrue("Current account is successfully verified",
                MyContextHolder.get().persistentAccounts().getCurrentAccount().getCredentialsVerified()
                == MyAccount.CredentialsVerificationStatus.SUCCEEDED);
    }

    public void checkDataPath() {
        if (!TextUtils.isEmpty(dataPath)) {
            assertEquals("Data path. " + MyContextHolder.get(), dataPath,
                    MyContextHolder.get().context().getDatabasePath("andstatus").getPath());
        }
    }

    public MyAccount getConversationMyAccount() {
        return getMyAccount(conversationAccountName);
    }

    @NonNull
    public MyAccount getMyAccount(String accountName) {
        MyAccount ma = MyContextHolder.get().persistentAccounts().fromAccountName(accountName);
        assertTrue(accountName + " exists", ma.isValid());
        assertTrue("Origin for " + accountName + " doesn't exist", ma.getOrigin().isValid());
        return ma;
    }

    @NonNull
    public Actor getAccountActorByOid(String actorOid) {
        for (MyAccount ma : MyContextHolder.get().persistentAccounts().list()) {
            if (ma.getActorOid().equals(actorOid)) {
                return ma.getActor();
            }
        }
        return Actor.EMPTY;
    }

    public long getConversationOriginId() {
        return getConversationMyAccount().getOriginId();
    }
}
