// PURPOSE: Shared empty-state layout for list and chart pages.
import { Box, Typography } from '@mui/material';

export default function EmptyState({ title, description }) {
  return (
    <Box sx={{ py: 6, px: 2, textAlign: 'center' }}>
      <Typography variant="subtitle1" fontWeight={600} gutterBottom>
        {title}
      </Typography>
      {description && (
        <Typography variant="body2" color="text.secondary" sx={{ maxWidth: 480, mx: 'auto' }}>
          {description}
        </Typography>
      )}
    </Box>
  );
}
