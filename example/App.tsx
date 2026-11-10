
import { useRef, useState } from 'react';
import {
  PermissionsAndroid,
  Platform,
  Pressable,
  StyleSheet,
  Text,
  View,
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
  const [status, setStatus] = useState('Ready to test');

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

      await PushGroupsKit.showGroupedNotificationAsync(
        `test-${Date.now()}-${id}`,
        groupId,
        title,
        `Test message #${id}`
      );

      setStatus(`Sent notification #${id} to ${groupId}`);
    } catch (error) {
      setStatus(
        error instanceof Error ? error.message : String(error)
      );
    }
  }

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Push Groups Kit</Text>

      <Text style={styles.subtitle}>
        Android notification grouping test
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

      <Text style={styles.status}>{status}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
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
});
