<script>
  import moment from 'moment';
  import * as R from 'ramda';

  import * as validation from '@Utility/validation';

  export let value;

  $: formattedDate = R.ifElse(
    R.compose(
      R.equals('Date'),
      R.type
    ),
    R.curry(date => moment(date).format(validation.DATE_FORMAT)),
    R.identity
  )(value);
</script>

{formattedDate}
