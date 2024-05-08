import * as React from 'react';
import { unstable_useForkRef as useForkRef } from '@mui/utils';
import Box from '@mui/material/Box';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { unstable_useDateField as useDateField } from '@mui/x-date-pickers/DateField';
import { useClearableField } from '@mui/x-date-pickers/hooks';
import Dayjs from 'dayjs';

const BrowserField = React.forwardRef((props, ref) => {
  const {
    disabled,
    id,
    inputRef,
    InputProps: { ref: containerRef, startAdornment, endAdornment } = {},
    sx,
    ...other
  } = props;

  const handleRef = useForkRef(containerRef, ref);

  return (
    <Box
      sx={{ ...(sx || {}), display: 'flex', alignItems: 'center' }}
      id={id}
      ref={handleRef}
    >
      {startAdornment}
      <input disabled={disabled} ref={inputRef} {...other} />
      {endAdornment}
    </Box>
  );
});

const BrowserDateField = React.forwardRef((props, ref) => {
  const { slots, slotProps, ...textFieldProps } = props;

  const fieldResponse = useDateField({
    ...textFieldProps,
    enableAccessibleFieldDOMStructure: false,
  });

  const processedFieldProps = useClearableField({
    ...fieldResponse,
    slots,
    slotProps,
  });

  return <BrowserField ref={ref} {...processedFieldProps} />;
});

const BrowserDatePicker = React.forwardRef((props, ref) => {
  const { onChange, ...otherProps } = props;
  return (
    <DatePicker
      ref={ref}
      {...otherProps}
      onChange={onChange}
      slots={{ ...otherProps.slots, field: BrowserDateField }}
      views={['year']}
      label="End Year" 
      minDate={Dayjs('2000')}
      maxDate={Dayjs('2024')}
      value={otherProps.value}
    />
  );
});

export default function BrowserV6Field(props) {
  return (
    <LocalizationProvider dateAdapter={AdapterDayjs}>
      <BrowserDatePicker
        slotProps={{
          field: { clearable: false },
        }}
        onChange={props.onChange} // Use the onChange function passed as a prop
        value={props.value}
      />
    </LocalizationProvider>
  );
}
