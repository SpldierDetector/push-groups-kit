
import { useEffect, useRef, useState } from 'react';
import {
  PermissionsAndroid,
  Platform,
  Pressable,
  StyleSheet,
  Text,
  ScrollView,
} from 'react-native';

import PushGroupsKit from 'push-groups-kit';

async function requestNotificationPermission(): Promise<boolean> {
  if (Platform.OS !== 'android') {
    return false;
  }

  if (Number(Platform.Version) < 33) {
    return true;
  }

  const result = await PermissionsAndroid.request(
    PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS
  );

  return result === PermissionsAndroid.RESULTS.GRANTED;
}

export default function App() {
  const nextMessageId = useRef(0);
  const lastMessageIds = useRef<Record<string, string>>({});
  const [status, setStatus] = useState('Ready to test');
  const [lastPress, setLastPress] = useState('No notification press received yet');

  useEffect(() => {
    let active = true;
    let receivedLiveEvent = false;

    const subscription = PushGroupsKit.addListener(
      'onNotificationPressed',
      (event) => {
        receivedLiveEvent = true;

        if (active) {
          setLastPress(
            `Event: ${JSON.stringify(event, null, 2)}`
          );
        }
      }
    );

    PushGroupsKit.getInitialNotificationPressAsync()
      .then((event) => {
        if (active && !receivedLiveEvent && event) {
          setLastPress(
            `Initial: ${JSON.stringify(event, null, 2)}`
          );
        }
      })
      .catch((error) => {
        if (active) {
          setLastPress(`Press error: ${String(error)}`);
        }
      });

    return () => {
      active = false;
      subscription.remove();
    };
  }, []);

  async function sendTestNotification(
    groupId: string,
    title: string
  ) {
    try {
      const hasPermission = await requestNotificationPermission();

      if (!hasPermission) {
        setStatus('Notification permission denied');
        return;
      }

      nextMessageId.current += 1;

      const id = nextMessageId.current;
      const notificationId = `test-${Date.now()}-${id}`;

      await PushGroupsKit.showGroupedNotificationAsync(
        notificationId,
        groupId,
        title,
        `Test message #${id}`
      );

      lastMessageIds.current[groupId] = notificationId;

      setStatus(`Sent notification #${id} to ${groupId}`);
    } catch (error) {
      setStatus(
        error instanceof Error ? error.message : String(error)
      );
    }
  }

  async function runDiagnostics() {
    try {
      const result = await PushGroupsKit.getNotificationDiagnosticsAsync();
      setStatus(JSON.stringify(result, null, 2));
    } catch (error) {
      setStatus(String(error));
    }
  }

  async function checkInitialPressAgain() {
    try {
      const result =
        await PushGroupsKit.getInitialNotificationPressAsync();

      setStatus(`Repeated initial press: ${JSON.stringify(result)}`);
    } catch (error) {
      setStatus (
        error instanceof Error ? error.message : String(error)
      );
    }
  }

  async function testUngroupedNotification() {
    try {
      await PushGroupsKit.showUngroupedDiagnosticAsync();
      setStatus('Notification scheduled. Press Home now.');
    } catch (error) {
      setStatus(String(error));
    }
  }

  async function dismissLastNotification(groupId: string) {
    const notificationId = lastMessageIds.current[groupId];

    if (!notificationId) {
      setStatus(`No tracked notification for ${groupId}`);
      return;
    }

    try {
      await PushGroupsKit.dismissNotificationAsync(
        notificationId,
        groupId
      );

      delete lastMessageIds.current[groupId];

      setStatus(`Dismissed last notification from ${groupId}`);
    } catch (error) {
      setStatus(
        error instanceof Error ? error.message : String(error)
      );
    }
  }

  async function dismissGroup(groupId: string) {
    try {
      await PushGroupsKit.dismissGroupAsync(groupId);

      delete lastMessageIds.current[groupId];

      setStatus(`Dismissed group ${groupId}`);
    } catch (error) {
      setStatus(
        error instanceof Error ? error.message : String(error)
      );
    }
  }

  async function dismissAll() {
    try {
      await PushGroupsKit.dismissAllAsync();

      lastMessageIds.current = {};

      setStatus('Dismissed all Push Groups Kit notifications');
    } catch (error) {
      setStatus(
        error instanceof Error ? error.message : String(error)
      );
    }
  }

  async function configureTestNotifications() {
    try {
      await PushGroupsKit.configureAsync({
        channelName: 'Test Notifications',
        summaryText: 'My grouped notifications',
      });

      setStatus('Notification configurtion saved');
    } catch (error) {
      setStatus(
        error instanceof Error ? error.message : String(error)
      );
    }
  }

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <Text style={styles.title}>Push Groups Kit</Text>

      <Text style={styles.subtitle}>
        Android notification grouping test
      </Text>

      <Text selectable style={styles.pressInfo}>
        {lastPress}
      </Text>

      <Pressable
        style={styles.button}
        onPress={() => sendTestNotification('chat-1', 'Alex')}
      >
        <Text style={styles.buttonText}>
          Send to Chat 1
        </Text>
      </Pressable>

      <Pressable
        style={styles.button}
        onPress={() => sendTestNotification('chat-2', 'Max')}
      >
        <Text style={styles.buttonText}>
          Send to Chat 2
        </Text>
      </Pressable>

      <Pressable
        style={styles.button}
        onPress={() => dismissLastNotification('chat-1')}
      >
        <Text style={styles.buttonText}>
          Dismiss Last from Chat 1
        </Text>
      </Pressable>

      <Pressable
        style={styles.button}
        onPress={() => dismissGroup('chat-1')}
      >
        <Text style={styles.buttonText}>
          Dismiss Chat 1
        </Text>
      </Pressable>

      <Pressable
        style={styles.button}
        onPress={dismissAll}
      >
        <Text style={styles.buttonText}>
          Dismiss All
        </Text>
      </Pressable>

      <Pressable
        style={styles.button}
        onPress={runDiagnostics}
      >
        <Text style={styles.buttonText}>
          Check Notification Channel
        </Text>
      </Pressable>

      <Pressable
        style={styles.button}
        onPress={configureTestNotifications}
      >
        <Text style={styles.buttonText}>
          Configure Notifications
        </Text>
      </Pressable>

      <Pressable
        style={styles.button}
        onPress={checkInitialPressAgain}
      >
        <Text style={styles.buttonText}>
          Check Initial Press Again
        </Text>
      </Pressable>

      <Pressable
        style={styles.button}
        onPress={testUngroupedNotification}
      >
        <Text style={styles.buttonText}>
          Test Without Grouping
        </Text>
      </Pressable>

      <Text style={styles.status}>{status}</Text>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flexGrow: 1,
    justifyContent: 'center',
    padding: 24,
    backgroundColor: '#101827',
  },
  title: {
    color: '#FFFFFF',
    fontSize: 26,
    fontWeight: 'bold',
    marginBottom: 8,
  },
  subtitle: {
    color: '#AEBCCE',
    fontSize: 15,
    marginBottom: 32,
  },
  button: {
    backgroundColor: '#208AEF',
    padding: 16,
    borderRadius: 12,
    marginBottom: 12,
  },
  buttonText: {
    color: '#FFFFFF',
    fontWeight: '600',
    textAlign: 'center',
  },
  status: {
    color: '#AEBCCE',
    marginTop: 20,
    textAlign: 'center',
  },
  pressInfo: {
    color: '#93e4c5',
    fontSize: 13,
    marginBottom: 16,
  },
});
