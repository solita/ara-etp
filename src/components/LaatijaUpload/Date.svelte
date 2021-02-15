<script>
  import * as dfns from 'date-fns';
  import * as R from 'ramda';

  import * as validation from '@Utility/validation';

  export let value;
</script>

{R.when(R.compose(R.equals('Date'), R.type), date =>
  dfns.format(date, validation.DATE_FORMAT)
)(value)}
