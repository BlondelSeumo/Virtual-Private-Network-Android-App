import Layout from "@app/components/layout";
import React from "react";
import {firestore} from "@app/services/firebase";
import css from 'styled-jsx/css'
import moment from "moment";
import {
  PieChart, Pie, ResponsiveContainer, Cell,
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend,
  AreaChart, Area,
} from 'recharts';
import {DatePicker} from "antd";

const styles = css.global`
  .custom-tooltip {
      width: 100px;
      height: fit-content;
      border-radius: 4px;
      padding: 6px;
      background-color: #424242;
      &__title {
        font-size: 9px;
        line-height: 1.56;
        letter-spacing: 1.35px;
        color: #9e9e9e;
      }
      &__content {
        font-size: 13px;
        font-weight: 600;
        line-height: 1.38;
        letter-spacing: 0.3px;
        color: #ffffff;
      }
  }
`

const dataPieChart = [
  {name: 'normal', value: 10},
  {name: 'gold_monthly', value: 4},
  {name: 'gold_yearly', value: 320},
];

const dataBarChart = [
  {
    name: '01', monthly: 4000, yearly: 2400,
  },
  {
    name: '02', monthly: 3000, yearly: 1398,
  },
  {
    name: '03', monthly: 2000, yearly: 9800,
  },
  {
    name: '04', monthly: 2780, yearly: 3908,
  },
  {
    name: '05', monthly: 1890, yearly: 4800,
  },
  {
    name: '06', monthly: 2390, yearly: 3800,
  },
  {
    name: '07', monthly: 3490, yearly: 4300,
  },
  {
    name: '08', monthly: 2000, yearly: 9800,
  },
  {
    name: '09', monthly: 2780, yearly: 3908,
  },
  {
    name: '10', monthly: 1890, yearly: 4800,
  },
  {
    name: '11', monthly: 2390, yearly: 3800,
  },
  {
    name: '12', monthly: 3490, yearly: 4300,
  },
];

const dataAreaChart = [
  {
    name: 'Page A', uv: 4000, pv: 2400, amt: 2400,
  },
  {
    name: 'Page B', uv: 3000, pv: 1398, amt: 2210,
  },
  {
    name: 'Page C', uv: 2000, pv: 9800, amt: 2290,
  },
  {
    name: 'Page D', uv: 2780, pv: 3908, amt: 2000,
  },
  {
    name: 'Page E', uv: 1890, pv: 4800, amt: 2181,
  },
  {
    name: 'Page F', uv: 2390, pv: 3800, amt: 2500,
  },
  {
    name: 'Page G', uv: 3490, pv: 4300, amt: 2100,
  },
];

const CustomTooltip = ({active, payload, label}) => {
  if (active) {
    return (
      <div className="custom-tooltip">
        {
          payload?.map((item, index) => (
            <div className="mb-2" key={index}>
              <div className="custom-tooltip__title uppercase">{item?.dataKey}</div>
              <div className="custom-tooltip__content uppercase">{item?.value}</div>
            </div>
          ))
        }
      </div>
    );
  }

  return null;
};

const COLORS = ['#89d34f', '#714fff', '#ff2e93'];

const RADIAN = Math.PI / 180;

const renderCustomizedLabel = ({
                                 cx, cy, midAngle, innerRadius, outerRadius, percent, index,
                               }) => {
  const radius = innerRadius + (outerRadius - innerRadius) * 0.5;
  const x = cx + radius * Math.cos(-midAngle * RADIAN);
  const y = cy + radius * Math.sin(-midAngle * RADIAN);

  return (percent * 100) > 10 ? (
    <text x={x} y={y} fill="white" textAnchor={x > cx ? 'start' : 'end'} dominantBaseline="central">
      {`${(percent * 100).toFixed(0)}%`}
    </text>
  ) : '';
};

const renderLegend = (props) => {
  const {payload} = props;

  return (
    <ul className="flex items-center justify-center w-full mt-3">
      {
        payload.map((entry, index) => {
          return (
            <li key={`item-${index}`} className={index < payload.length ? "mr-10" : ""}>
              <div className="flex items-center">
                <div style={{width: 10, height: 10, background: entry?.color, borderRadius: 10}}></div>
                <div className="ml-3">
                  {entry?.value?.split(" ")?.[0] || ""}
                </div>
              </div>
            </li>
          )
        })
      }
    </ul>
  );
}

const Index = () => {
  const [users, setUser] = React.useState([])
  const [currentYear, setYear] = React.useState(moment().year())
  const [currentMonth, setMonth] = React.useState({
    year: moment().year(),
    month: moment().month() + 1
  })
  const [anonymous, setAnonymous] = React.useState([])
  const [servers, setServer] = React.useState([])
  const [pieChartValues, setPieChartValues] = React.useState({})

  React.useEffect(() => {
    firestore.collection("users").get()
      .then((result) => {
        let rows = []

        result.forEach((row) => rows.push({id: row?.id, ...row?.data()}))

        setUser(rows)
      })
      .catch((err) => {

      })

    firestore.collection("anonymous").get()
      .then((result) => {
        let rows = []

        result.forEach((row) => rows.push({id: row?.id, ...row?.data()}))

        setAnonymous(rows)
      })
      .catch((err) => {

      })

    firestore.collection("Servers").get()
      .then((result) => {
        let rows = []

        result.forEach((row) => rows.push({id: row?.id, ...row?.data()}))

        setServer(rows)
      })
      .catch((err) => {

      })
  }, [])

  const todayCreatedUser = () => {
    const currentDate = moment().format("DD-MM-YYYY")

    return users?.reduce((total, item) => {
      const createdAt = moment(item.createAt || new Date(), "HH:mm DD/MM/YYYY").format("DD-MM-YYYY")
      if (currentDate === createdAt) {
        return total + 1
      }

      return total
    }, 0)
  }

  const totalDownloadAndUpload = () => {
    return [...users, ...anonymous]?.reduce((total, item) => {
      return {
        download: total?.download + (item?.traffic?.download || 0),
        upload: total?.upload + (item?.traffic?.upload || 0),
      }

    }, {download: 0, upload: 0})
  }

  const formatBytes = (bytes, decimals = 2) => {
    if (bytes === 0) return '0 Bytes';

    const k = 1024;
    const dm = decimals < 0 ? 0 : decimals;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];

    const i = Math.floor(Math.log(bytes) / Math.log(k));

    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
  }

  const pieChart = () => {
    const tmp = [...users, ...anonymous]

    return tmp.reduce((obj, item) => {
      if (!item?.premium) {
        return {
          ...obj,
          normal: {
            ...obj?.normal,
            value: obj?.normal?.value + 1
          }
        }
      }

      if (item?.premium?.productId && item?.premium?.productId === "gold_monthly") {
        return {
          ...obj,
          gold_monthly: {
            ...obj?.gold_monthly,
            value: obj?.gold_monthly?.value + 1
          }
        }
      }

      if (item?.premium?.productId && item?.premium?.productId === "gold_yearly") {
        return {
          ...obj,
          gold_yearly: {
            ...obj?.gold_yearly,
            value: obj?.gold_yearly?.value + 1
          }
        }
      }

      return obj
    }, {
      normal: {name: 'normal', value: 0},
      gold_monthly: {name: 'gold monthly', value: 0},
      gold_yearly: {name: 'gold yearly', value: 0},
    })
  }

  const lineChart = () => {
    let listMonths = {}

    for (let i = 1; i < 13; i++) {
      listMonths = {
        ...listMonths,
        [`${i < 10 ? "0" + i : i}/${currentYear}`]: {
          ...listMonths?.[`${i < 10 ? "0" + i : i}/${currentYear}`] || {},
          name: `${i < 10 ? "0" + i : i}/${currentYear}`,
          monthly: 0,
          yearly: 0,
          free: 0,
        }
      }
    }

    const sumUsers = [...users, ...anonymous]

    return sumUsers.reduce((obj, user) => {
      const tmpDate = moment(user?.lastLogin, "HH:mm DD/MM/YYYY")
      const tmpCreateAdDate = moment(user?.createAt, "HH:mm DD/MM/YYYY")

      let tmpMonthYear = ""

      if (tmpDate.isValid()) {
        tmpMonthYear = tmpDate.format("MM/YYYY")
      }

      if (tmpCreateAdDate.isValid()) {
        tmpMonthYear = tmpCreateAdDate.format("MM/YYYY")
      }

      if ((tmpDate.isValid() || tmpCreateAdDate.isValid()) && obj?.[tmpMonthYear]) {
        if (user?.premium?.productId && user?.premium?.productId === "gold_monthly") {
          return {
            ...obj,
            [tmpMonthYear]: {
              ...obj?.[tmpMonthYear] || {},
              monthly: obj?.[tmpMonthYear]?.monthly + 1
            }
          }
        } else if (user?.premium?.productId && user?.premium?.productId === "gold_yearly") {
          return {
            ...obj,
            [tmpMonthYear]: {
              ...obj?.[tmpMonthYear] || {},
              yearly: obj?.[tmpMonthYear]?.yearly + 1
            }
          }
        } else if (!user?.premium?.productId && obj?.[tmpMonthYear]) {
          return {
            ...obj,
            [tmpMonthYear]: {
              ...obj?.[tmpMonthYear] || {},
              free: obj?.[tmpMonthYear]?.free + 1
            }
          }
        }
      }

      return obj
    }, listMonths)
  }

  const onChangeYear = (e) => {
    setYear(moment(e).year())
  }

  const onChangeMonth = (e) => {
    setMonth({
      month: moment(e).month() + 1,
      year: moment(e).year()
    })
  }

  const areaChart = () => {

    const tmpYear = currentMonth?.year
    const month = currentMonth?.month
    const tmpMonthYear = `${month < 10 ? "0" + month : month}-${tmpYear}`

    const endOfMonth = moment(`01-${month < 10 ? "0" + month : month}-${tmpYear}`, "DD-MM-YYYY").endOf('month').date();

    let listDates = {}

    for (let i = 1; i <= endOfMonth; i++) {
      listDates = {
        ...listDates,
        [`${i}`]: {
          name: `${i}`,
          value: 0,
        }
      }
    }

    return [...users, ...anonymous].reduce((obj, item) => {
      let tmpDate = moment(item?.createAt, "HH:mm DD/MM/YYYY")

      if (!tmpDate.isValid()) {
        tmpDate = moment(item?.lastLogin, "HH:mm DD/MM/YYYY")
      }

      if (tmpDate.isValid() && tmpDate?.format("MM-YYYY") === tmpMonthYear) {
        const tmpMonth = tmpDate.date()
        return {
          ...obj,
          [`${tmpMonth}`]: {
            ...obj?.[tmpMonth] || {},
            value: (obj?.[tmpMonth]?.value || 0) + 1,
          }
        }
      }

      return obj
    }, listDates)
  }

  const tmpDataPieChart = pieChart()
  const newAreaChartValue = Object.values(areaChart())
  const totalDownloadUpload = totalDownloadAndUpload()
  const tmpLineChart = Object.values(lineChart())

  const dataForPieChart = () => {
    const total = (tmpDataPieChart?.normal?.value + tmpDataPieChart?.gold_monthly?.value + tmpDataPieChart?.gold_yearly?.value) || 1

    return {
      normal: (((tmpDataPieChart?.normal?.value || 0) * 100)/total).toFixed(0),
      gold_monthly: (((tmpDataPieChart?.gold_monthly?.value || 0) * 100)/total).toFixed(0),
      gold_yearly: (((tmpDataPieChart?.gold_yearly?.value || 0) * 100)/total).toFixed(0),
    }
  }

  return (
    <Layout title="Home">
      <div className="flex">
        <div className="core-card flex-1 mr-4 h-24">
          <div className="pa-10 uppercase second-text-color">today users created</div>
          <div className="title-2 uppercase text-black mt-2">{todayCreatedUser()}</div>
        </div>
        <div className="core-card flex-1 mr-4 h-24">
          <div className="pa-10 uppercase second-text-color">total of users</div>
          <div className="title-2 uppercase text-black mt-2">{((users?.length || 0) + (anonymous?.length || 0))}</div>
        </div>
        <div className="core-card flex-1 mr-4 h-24">
          <div className="pa-10 uppercase second-text-color">happening servers</div>
          <div className="title-2 uppercase text-black mt-2">{servers?.length}</div>
        </div>
        <div className="core-card flex-1 h-24 mr-4">
          <div className="pa-10 uppercase second-text-color">total download</div>
          <div className="title-2 uppercase text-black mt-2">{formatBytes(totalDownloadUpload?.download || 0)}</div>
        </div>
        <div className="core-card flex-1 h-24">
          <div className="pa-10 uppercase second-text-color">total upload</div>
          <div className="title-2 uppercase text-black mt-2">{formatBytes(totalDownloadUpload?.upload || 0)}</div>
        </div>
      </div>
      <div className="flex flex-wrap -mx-4">
        <div className="p-4 w-5/12">
          <div className="core-card w-full p-8" style={{maxHeight: 400}}>
            <div className="font-bold pa-14 text-black mb-4">User Status</div>
            <div className="flex">
              <div style={{width: '100%', height: 300}} className="flex-1 overflow-hidden">
                <ResponsiveContainer>
                  <PieChart>
                    <Pie
                      isAnimationActive={false}
                      innerRadius={0}
                      outerRadius={135}
                      data={Object.values(tmpDataPieChart)}
                      labelLine={false}
                      label={renderCustomizedLabel}
                      fill="#8884d8"
                      dataKey="value"
                    >
                      {
                        Object.values(tmpDataPieChart).map((entry, index) => <Cell key={`cell-${index}`}
                                                                    fill={COLORS[index % COLORS.length]}/>)
                      }
                    </Pie>
                  </PieChart>
                </ResponsiveContainer>
              </div>
              <div className="flex-1 flex items-start justify-end">
                <ul>
                  <li className="mb-4 pa-13 font-medium flex items-center">
                    <div style={{width: 10, height: 10, background: "#89d34f", borderRadius: 10}}></div>
                    <div className="ml-4">
                      <div>Active and unknown:</div>
                      <div>{tmpDataPieChart?.normal?.value} ({dataForPieChart()?.normal}%)</div>
                    </div>
                  </li>
                  <li className="mb-4 pa-13 font-medium flex items-center">
                    <div style={{width: 10, height: 10, background: "#714fff", borderRadius: 10}}></div>
                    <div className="ml-4">
                      <div>Monthly Subscription:</div>
                      <div>{tmpDataPieChart?.gold_monthly?.value} ({dataForPieChart()?.gold_monthly}%)</div>
                    </div>
                  </li>
                  <li className="mb-4 pa-13 font-medium flex items-center">
                    <div style={{width: 10, height: 10, background: "#ff2e93", borderRadius: 10}}></div>
                    <div className="ml-4">
                      <div>Yearly Subscription:</div>
                      <div>{tmpDataPieChart?.gold_yearly?.value} ({dataForPieChart()?.gold_yearly}%)</div>
                    </div>
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </div>
        <div className="p-4 w-7/12">
          <div className="core-card w-full p-8" style={{maxHeight: 400}}>
            <div className="flex items-center justify-between  mb-4">
              <div className="font-bold pa-14 text-black">Active Users</div>
              <DatePicker allowClear={false} picker="year" value={moment(`01-01-${currentYear}`, "DD-MM-YYYY")}
                          onChange={onChangeYear}/>
            </div>
            <div style={{width: '100%', height: 300}} className="flex-1">
              <ResponsiveContainer>
                <BarChart
                  isAnimationActive={false}
                  data={tmpLineChart}
                  margin={{
                    top: 20, right: 30, left: 20, bottom: 5,
                  }}
                >
                  <CartesianGrid strokeDasharray="3 3"/>
                  <XAxis dataKey="name"/>
                  <YAxis/>
                  <Tooltip content={<CustomTooltip/>}/>
                  <Legend content={renderLegend}/>
                  <Bar name="Monthly subscription" dataKey="yearly" stackId="a" fill="#ff2e93"/>
                  <Bar name="Yearly subscription" dataKey="monthly" stackId="a" fill="#714fff"/>
                  <Bar name="Free subscription" dataKey="free" stackId="a" fill="#89d34f"/>
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>
        </div>
      </div>
      <div className="flex flex-wrap">
        <div className="core-card w-full p-8" style={{maxHeight: 400}}>
          <div className="flex items-center justify-between  mb-4">
            <div className="font-bold pa-14 text-black">Users by Month</div>
            <DatePicker
              value={moment(`01-${currentMonth?.month < 10 ? "0" + currentMonth?.month : currentMonth?.month}-${currentMonth?.year}`, "DD-MM-YYYY")}
              allowClear={false} picker="month" onChange={onChangeMonth}/>
          </div>
          <div style={{width: '100%', height: 300}} className="flex-1">
            <ResponsiveContainer>
              <AreaChart
                data={newAreaChartValue}
                margin={{
                  top: 10, right: 30, left: 0, bottom: 0,
                }}
              >
                <CartesianGrid strokeDasharray="3 3"/>
                <XAxis dataKey="name"/>
                <YAxis/>
                <Tooltip content={<CustomTooltip/>}/>
                <Area stackId="name" dot={{stroke: '#714fff', strokeWidth: 4}}
                      type="linear" dataKey="value" stroke="#714fff"
                      fill="rgba(113, 79, 255, 0.2)"/>
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>
      <style jsx>{styles}</style>
    </Layout>
  )
}

export default Index