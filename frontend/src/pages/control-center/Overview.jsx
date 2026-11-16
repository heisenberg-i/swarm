// PURPOSE: Control Center page for tenant onboarding and plant assignment.
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box, Typography, Card, CardActionArea, CardContent, Grid, CircularProgress, Alert, Chip,
} from '@mui/material';
import { controlCenterAPI } from '../../services/api';

const tiles = [
  { key: 'companiesTotal', label: 'Tenants', path: '/companies', hint: 'Companies buying Swarm' },
  { key: 'tenantsReady', label: 'Ready to operate', path: '/companies?filter=READY', hint: 'Admin + at least one plant' },
  { key: 'tenantsNeedAdmin', label: 'Need Super Admin', path: '/companies?filter=NEEDS_ADMIN', hint: 'Cannot log in to the app yet' },
  { key: 'tenantsNeedPlants', label: 'Need plants', path: '/companies?filter=NEEDS_PLANTS', hint: 'Tenant exists, no sites assigned' },
  { key: 'plantsAssigned', label: 'Assigned plants', path: '/plants?tab=assigned', hint: 'Sites live under a tenant' },
  { key: 'plantsUnassigned', label: 'Unassigned pool', path: '/plants?tab=unassigned', hint: 'Create then attach to a company' },
  { key: 'pendingApprovals', label: 'User approvals', path: '/approvals', hint: 'Sign-ups waiting for a tenant' },
  { key: 'plantsHmiEnabled', label: 'HMI enabled', path: '/hmi-access', hint: 'Sites allowed to open Plant HMI' },
];

export default function Overview() {
  const navigate = useNavigate();
  const [data, setData] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    controlCenterAPI.overview()
      .then(({ data: res }) => setData(res.data))
      .catch((e) => setError(e.response?.data?.message || 'Failed to load overview'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <CircularProgress />;

  return (
    <Box>
      <Typography variant="h5" fontWeight={700}>Tenant onboarding</Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 3, maxWidth: 720 }}>
        This Control Center provisions Swarm customers at scale: company → Super Admin → plants.
        Plant monitoring and sensors stay in the Swarm app. Plant HMI is shown there only after you enable it under <strong>HMI</strong>.
      </Typography>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      <Grid container spacing={2}>
        {tiles.map((tile) => (
          <Grid item xs={12} sm={6} md={4} key={tile.key}>
            <Card>
              <CardActionArea onClick={() => navigate(tile.path)} sx={{ height: '100%' }}>
                <CardContent>
                  <Typography variant="h4" fontWeight={700}>{data?.[tile.key] ?? '—'}</Typography>
                  <Typography variant="subtitle1" fontWeight={600}>{tile.label}</Typography>
                  <Typography variant="caption" color="text.secondary">{tile.hint}</Typography>
                </CardContent>
              </CardActionArea>
            </Card>
          </Grid>
        ))}
      </Grid>
      {data?.companiesDisabled > 0 && (
        <Chip sx={{ mt: 3 }} label={`${data.companiesDisabled} disabled tenant(s)`} onClick={() => navigate('/companies?filter=DISABLED')} />
      )}
    </Box>
  );
}
