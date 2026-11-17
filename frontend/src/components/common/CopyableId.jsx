// PURPOSE: Reusable React component for the SWARM UI.
import { Box, IconButton, Stack, Tooltip, Typography } from '@mui/material';
import { ContentCopy } from '@mui/icons-material';
import { useState } from 'react';

export default function CopyableId({ label, value, mono = true }) {
  const [copied, setCopied] = useState(false);
  if (value == null || value === '') {
    return (
      <Box>
        {label && <Typography variant="caption" color="text.secondary">{label}</Typography>}
        <Typography variant="body2">—</Typography>
      </Box>
    );
  }
  const text = String(value);
  const copy = async (event) => {
    event?.stopPropagation?.();
    try {
      await navigator.clipboard.writeText(text);
      setCopied(true);
      setTimeout(() => setCopied(false), 1500);
    } catch {
      setCopied(false);
    }
  };
  return (
    <Box>
      {label && <Typography variant="caption" color="text.secondary">{label}</Typography>}
      <Stack direction="row" alignItems="center" spacing={0.5}>
        <Typography
          variant="body2"
          sx={{ fontFamily: mono ? 'ui-monospace, SFMono-Regular, Menlo, monospace' : 'inherit', wordBreak: 'break-all' }}
        >
          {text}
        </Typography>
        <Tooltip title={copied ? 'Copied' : 'Copy'}>
          <IconButton size="small" onClick={copy} aria-label={`Copy ${label || 'value'}`}>
            <ContentCopy fontSize="inherit" />
          </IconButton>
        </Tooltip>
      </Stack>
    </Box>
  );
}
