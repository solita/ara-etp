module.exports = {
  theme: {
    colors: {
      primary: '#538000',
      secondary: '#2e5053',
      tertiary: '#f1efe4',
      dark: '#000',
      light: '#fff',
      primarydark: '#345000',
      secondarydark: '#343841',
      link: '#538000',
      hover: '#538000',
      althover: '#c5e191',
      focus: '#538000',
      active: '#6d6d6d',
      disabled: '#a7a7a7',
      lighterdisabled: '#f1efe4',
      error: '#9e0000',
      warning: '#ead049',
      success: '#538000',
      background: '#eee',
      backgroundhalf: 'rgba(238, 238, 238, 0.5)',
      hr: 'rgba(52, 56, 65, 0.3)',
      transparent: 'rgba(0,0,0,0)',
      tableborder: 'rgba(167,167,167,0.3)',
      beige: '#f2f8e7'
    },
    extend: {
      fontFamily: {
        display: ['Montserrat', 'sans-serif'],
        body: ['Montserrat', 'sans-serif'],
        icon: 'Material Icons'
      },
      borderWidth: {
        '1': '1px',
        '3': '3px',
        '7': '7px'
      },
      inset: {
        '100': '100%',
        '3em': '3em'
      },
      opacity: {
        '15': '0.15'
      },
      boxShadow: {
        'hover-2-primary': '0 0.5em #538000',
        'hover-2-secondary': '0 0.5em #2e5053',
        dropdownlist: '0 25px 50px 0px rgba(0, 0, 0, 0.25)'
      },
      maxWidth: {
        '7em': '7em',
        '70': '70%',
        '1280': '1280px',
        '1440': '1440px'
      },
      minWidth: {
        '10': '10em'
      },
      width: {
        sm: '640px',
        md: '768px',
        lg: '1024px',
        xl: '1280px'
      },
      minHeight: {
        '85': '85vh',
        '2.5em': '2.5em',
        '3em': '3em'
      },
      letterSpacing: {
        xl: '0.15em'
      }
    }
  },
  variants: {},
  plugins: [],
  future: {
    removeDeprecatedGapUtilities: true
  },
  purge: {
    content: ['./src/**/*.css', './src/**/*.svelte'],
    options: { safelist: { standard: /svelte-/ } }
  }
};
