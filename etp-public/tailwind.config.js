module.exports = {
  theme: {
    colors: {
      black: '#000000',
      grey: '#eaeded',
      white: '#ffffff',
      lightgreen: '#c5e191',
      green: '#538000',
      altgreen: '#345000',
      darkgreen: '#466B00',
      ashblue: '#2e5053',
      darkashblue: '#343841',
      beige: '#f2f8e7',
      lightbeige: '#f1efe4',
      lightgrey: '#e0e0e0',
      darkgrey: '#757575',
      red: '#9E0000'
    },
    extend: {
      fontFamily: {
        display: ['Montserrat', 'sans-serif'],
        body: ['Montserrat', 'sans-serif'],
        icon: 'Material Icons'
      },
      flex: {
        full: '1 1 100%'
      },
      boxShadow: {
        lightgreen: '0px 6px 0px 0px #c5e191',
        green: '0px 6px 0px 0px #538000',
        altgreen: '0px 6px 0px 0px #345000',
        ashblue: '0px 6px 0px 0px #2e5053'
      },
      screens: {
        xs: '400px',
        xxl: '1440px',
        print: { raw: 'print' }
      }
    },
    fontSize: {
      // Tailwind V1 theming. Feel free to change if you are a visual designer
      xs: '0.75rem',
      sm: '0.875rem',
      base: '1rem',
      lg: '1.125rem',
      xl: '1.25rem',
      '2xl': '1.5rem',
      '3xl': '1.875rem',
      '4xl': '2.25rem',
      '5xl': '3rem',
      '6xl': '4rem'
    }
  },
  plugins: [],
  content: ['./src/**/*.svelte'],
};
