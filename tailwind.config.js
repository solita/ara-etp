module.exports = {
  theme: {
    colors: {
      primary: '#538000',
      secondary: '#2e5053',
      dark: '#000',
      light: '#fff',
      primarydark: '#345000',
      secondarydark: '#343841',
      link: '#538000',
      hover: '#538000',
      focus: '#538000',
      active: '#6d6d6d',
      disabled: '#a7a7a7',
      error: '#9e0000',
      success: '#538000',
      background: '#eee'
    },
    extend: {
      fontFamily: {
        display: ['Montserrat', 'sans-serif'],
        body: ['Montserrat', 'sans-serif'],
        icon: 'Material Icons'
      },
      borderWidth: {
        '1': '1px',
        '3': '3px'
      },
      inset: {
        '100': '100%'
      },
      boxShadow: {
        'hover-2-primary': '0 0.5em #538000',
        'hover-2-secondary': '0 0.5em #2e5053'
      },
      maxWidth: {
        '70': '70%',
        '1280': '1280'
      },
      minHeight: {
        '85': '85vh',
        '2.5em': '2.5em',
        '3em': '3em'
      }
    }
  },
  variants: {},
  plugins: []
};
