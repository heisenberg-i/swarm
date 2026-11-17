// PURPOSE: Control Center page for MQTT ingestion status.
import { useEffect, useState } from 'react';
import {
  Alert, Box, Button, Card, CardContent, Chip, CircularProgress, Grid, Table, TableBody,
  TableCell, TableHead, TableRow, Typography,
} from '@mui/material';
import { controlCenterAPI } from '../../services/api';
import CopyableId from '../../components/common/CopyableId';

export default function IotStatus() {
  const [status, setStatus] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  const load = () => {
    controlCenterAPI.iotStatus()
      .then(({ data }) => {
        setStatus(data.data);
        setError('');
      })
      .catch((e) => setError(e.response?.data?.message || 'Failed to load IoT status'))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    load();
    const timer = setInterval(load, 15000);
    return () => clearInterval(timer);
  }, []);

  if (loading && !status) return <CircularProgress />;

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
        <Box>
          <Typography variant="h5" fontWeight={700}>MQTT / IoT</Typography>
          <Typography variant="body2" color="text.secondary">
            Broker connectivity, supported topics, and recent rejected ingest attempts. Device secrets are never shown here.
          </Typography>
        </Box>
        <Button onClick={load}>Refresh</Button>
      </Box>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {status && (
        <Grid container spacing={2}>
          <Grid item xs={12} md={6}>
            <Card>
              <CardContent>
                <Typography variant="h6" sx={{ mb: 1 }}>Broker</Typography>
                <StackChips status={status} />
                <CopyableId label="Broker URL" value={status.brokerUrl} />
                <CopyableId label="SWARM v1 topic" value={status.swarmTopicPattern} />
                <CopyableId label="Legacy topic" value={status.legacyTopicPattern} />
                <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                  Protocol {status.protocolVersion}. Messages received {status.messagesReceived}. Failures {status.failures}.
                </Typography>
                {status.lastError && (
                  <Alert severity="warning" sx={{ mt: 2 }}>{status.lastError}</Alert>
                )}
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={12} md={6}>
            <Card>
              <CardContent>
                <Typography variant="h6" sx={{ mb: 1 }}>Registry</Typography>
                <Typography variant="body2">{status.registeredDevices} registered device(s)</Typography>
                <Typography variant="body2">{status.devicesSeen} with last-seen timestamps</Typography>
                <Typography variant="caption" color="text.secondary" display="block" sx={{ mt: 1 }}>
                  Last message {status.lastMessageAt || 'none yet'}. Last connected {status.lastConnectedAt || 'n/a'}.
                </Typography>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={12}>
            <Card>
              <CardContent>
                <Typography variant="h6" sx={{ mb: 1 }}>Recent ingest rejections</Typography>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>When</TableCell>
                      <TableCell>Device</TableCell>
                      <TableCell>Plant</TableCell>
                      <TableCell>Reason</TableCell>
                      <TableCell>Detail</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {(status.recentRejections || []).length === 0 && (
                      <TableRow><TableCell colSpan={5}>No rejected messages recorded.</TableCell></TableRow>
                    )}
                    {(status.recentRejections || []).map((row, index) => (
                      <TableRow key={`${row.receivedAt}-${index}`}>
                        <TableCell>{row.receivedAt || '—'}</TableCell>
                        <TableCell>{row.deviceUid || '—'}</TableCell>
                        <TableCell>{row.plantId || '—'}</TableCell>
                        <TableCell>{row.reasonCode}</TableCell>
                        <TableCell>{row.reasonDetail}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      )}
    </Box>
  );
}

function StackChips({ status }) {
  return (
    <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', mb: 2 }}>
      <Chip size="small" color={status.enabled ? 'success' : 'default'} label={status.enabled ? 'Enabled' : 'Disabled'} />
      <Chip size="small" color={status.configured ? 'success' : 'default'} label={status.configured ? 'Configured' : 'Not configured'} />
      <Chip size="small" color={status.connected ? 'success' : 'warning'} label={status.connected ? 'Connected' : 'Disconnected'} />
    </Box>
  );
}
