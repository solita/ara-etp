import Litepicker from 'litepicker';

export const litepicker = (node, opts) => {
  const input = node.querySelector('input');

  const options = {
    element: input,
    format: 'D.M.YYYY',
    singleMode: true,
    lang: opts.lang,
    dropdowns: {
      minYear: 2013,
      maxYear: new Date().getFullYear() + 11,
      months: true,
      years: true
    }
  };

  const picker = new Litepicker(options);
  picker.on('selected', wrapper => {
    opts.update(wrapper.dateInstance);
  });

  return () => picker.destroy();
};
